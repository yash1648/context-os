# State Management

## Architecture Overview

ContextOS uses a **split state management** approach:

| State Type | Tool | Purpose |
|---|---|---|
| Server state | TanStack Query | API data caching, pagination, optimistic updates |
| Client state | Zustand | UI state, auth tokens, sidebar, theme |
| Form state | React Hook Form | Form field state, validation |
| URL state | React Router | Route params, search params |

## TanStack Query Configuration

```typescript
// src/main.tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,        // 5 minutes
      gcTime: 30 * 60 * 1000,           // 30 minutes (cache retention)
      retry: 2,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
    },
    mutations: {
      retry: 1,
    },
  },
});
```

## Query Key Convention

```typescript
// src/hooks/useContainers.ts
export const containerKeys = {
  all: ['containers'] as const,
  lists: () => [...containerKeys.all, 'list'] as const,
  list: (filters: ContainerFilters) => [...containerKeys.lists(), filters] as const,
  details: () => [...containerKeys.all, 'detail'] as const,
  detail: (id: string) => [...containerKeys.details(), id] as const,
  byType: (type: ContainerType) => [...containerKeys.all, 'type', type] as const,
  search: (query: string) => [...containerKeys.all, 'search', query] as const,
};

// Usage in hooks
export function useContainers(filters: ContainerFilters) {
  return useQuery({
    queryKey: containerKeys.list(filters),
    queryFn: () => containerService.list(filters),
  });
}

export function useContainer(id: string) {
  return useQuery({
    queryKey: containerKeys.detail(id),
    queryFn: () => containerService.getById(id),
    enabled: !!id,
  });
}

export function useCreateContainer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: containerService.create,
    onSuccess: (newContainer) => {
      // Invalidate list queries
      queryClient.invalidateQueries({ queryKey: containerKeys.lists() });
      
      // Pre-populate detail cache
      queryClient.setQueryData(
        containerKeys.detail(newContainer.id),
        newContainer
      );
      
      // Show success toast
      toast.success('Container created');
    },
    onError: (error) => {
      toast.error(error.message);
    },
  });
}
```

## Optimistic Updates

```typescript
export function useUpdateProgress(containerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (progress: number) =>
      containerService.updateProgress(containerId, progress),

    onMutate: async (newProgress) => {
      // Cancel outgoing queries
      await queryClient.cancelQueries({
        queryKey: containerKeys.detail(containerId),
      });

      // Snapshot previous value
      const previous = queryClient.getQueryData<ContainerDetail>(
        containerKeys.detail(containerId)
      );

      // Optimistically update
      if (previous) {
        queryClient.setQueryData<ContainerDetail>(
          containerKeys.detail(containerId),
          { ...previous, progressPercentage: newProgress }
        );
      }

      return { previous };
    },

    onError: (_err, _newProgress, context) => {
      // Rollback on error
      if (context?.previous) {
        queryClient.setQueryData(
          containerKeys.detail(containerId),
          context.previous
        );
      }
      toast.error('Failed to update progress');
    },

    onSettled: () => {
      // Refetch to ensure consistency
      queryClient.invalidateQueries({
        queryKey: containerKeys.detail(containerId),
      });
    },
  });
}
```

## Infinite Query (Search)

```typescript
export function useSearch(query: string, filters: SearchFilters) {
  return useInfiniteQuery({
    queryKey: ['search', query, filters],
    queryFn: ({ pageParam = 0 }) =>
      searchService.search(query, { ...filters, offset: pageParam, limit: 20 }),
    getNextPageParam: (lastPage, allPages) => {
      const totalFetched = allPages.reduce((sum, p) => sum + p.data.length, 0);
      return totalFetched < lastPage.total ? totalFetched : undefined;
    },
    enabled: query.length >= 2,
    staleTime: 60 * 1000, // 1 minute for search results
  });
}
```

## Zustand Stores

### Auth Store

```typescript
// src/stores/authStore.ts
interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;

  // Actions
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  setAccessToken: (token: string) => void;
  loadFromStorage: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      login: async (email, password) => {
        const response = await authService.login(email, password);
        set({
          user: response.user,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          isAuthenticated: true,
        });
      },

      logout: () => {
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        });
        // Clear query cache
        queryClient.clear();
      },

      setAccessToken: (token) => set({ accessToken: token }),
      loadFromStorage: () => {
        // Hydrate from localStorage (persist middleware handles this)
      },
    }),
    {
      name: 'contextos-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
    }
  )
);
```

### UI Store

```typescript
// src/stores/uiStore.ts
interface UIState {
  sidebarOpen: boolean;
  theme: 'light' | 'dark' | 'system';
  activeModal: string | null;
  toasts: Toast[];

  // Actions
  toggleSidebar: () => void;
  setTheme: (theme: 'light' | 'dark' | 'system') => void;
  openModal: (id: string) => void;
  closeModal: () => void;
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set, get) => ({
      sidebarOpen: true,
      theme: 'system',
      activeModal: null,
      toasts: [],

      toggleSidebar: () => set((s) => ({ sidebarOpen: !s.sidebarOpen })),
      setTheme: (theme) => set({ theme }),
      openModal: (id) => set({ activeModal: id }),
      closeModal: () => set({ activeModal: null }),

      addToast: (toast) =>
        set((s) => ({
          toasts: [...s.toasts, { ...toast, id: crypto.randomUUID() }],
        })),

      removeToast: (id) =>
        set((s) => ({
          toasts: s.toasts.filter((t) => t.id !== id),
        })),
    }),
    {
      name: 'contextos-ui',
      partialize: (state) => ({
        theme: state.theme,
        sidebarOpen: state.sidebarOpen,
      }),
    }
  )
);
```

### Search Store

```typescript
// src/stores/searchStore.ts
interface SearchState {
  query: string;
  recentSearches: string[];
  filters: SearchFilters;

  setQuery: (query: string) => void;
  addRecentSearch: (query: string) => void;
  clearRecentSearches: () => void;
  setFilters: (filters: Partial<SearchFilters>) => void;
  resetFilters: () => void;
}

export const useSearchStore = create<SearchState>()(
  persist(
    (set, get) => ({
      query: '',
      recentSearches: [],
      filters: {
        types: [],
        status: [],
        tags: [],
        dateFrom: null,
        dateTo: null,
      },

      setQuery: (query) => set({ query }),

      addRecentSearch: (query) =>
        set((s) => ({
          recentSearches: [
            query,
            ...s.recentSearches.filter((q) => q !== query),
          ].slice(0, 10),
        })),

      clearRecentSearches: () => set({ recentSearches: [] }),

      setFilters: (filters) =>
        set((s) => ({ filters: { ...s.filters, ...filters } })),

      resetFilters: () =>
        set({
          filters: { types: [], status: [], tags: [], dateFrom: null, dateTo: null },
        }),
    }),
    {
      name: 'contextos-search',
      partialize: (state) => ({
        recentSearches: state.recentSearches,
      }),
    }
  )
);
```

## WebSocket Integration with TanStack Query

```typescript
// src/hooks/useWebSocket.ts
export function useWebSocketSubscription(containerId: string) {
  const queryClient = useQueryClient();

  useEffect(() => {
    const unsubscribe = wsManager.subscribeToContainer(
      containerId,
      (update: ContainerUpdateMessage) => {
        // Update detail query cache
        queryClient.setQueryData(
          containerKeys.detail(containerId),
          (old: ContainerDetail | undefined) => {
            if (!old) return old;
            return {
              ...old,
              ...update.payload,
              updatedAt: update.timestamp,
            };
          }
        );

        // Invalidate list queries
        queryClient.invalidateQueries({
          queryKey: containerKeys.lists(),
          refetchType: 'none', // Don't refetch, just mark stale
        });
      }
    );

    return unsubscribe;
  }, [containerId, queryClient]);
}
```
