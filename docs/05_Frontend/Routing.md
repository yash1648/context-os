# Routing

## Route Configuration

```typescript
// src/routes/routePaths.ts
export const ROUTES = {
  // Public
  LOGIN: '/login',
  REGISTER: '/register',
  FORGOT_PASSWORD: '/forgot-password',
  
  // Protected
  DASHBOARD: '/',
  CONTAINERS: '/containers',
  CONTAINER_DETAIL: '/containers/:id',
  CONTAINER_CREATE: '/containers/new',
  CONTAINER_EDIT: '/containers/:id/edit',
  TAGS: '/tags',
  SEARCH: '/search',
  SEARCH_QUERY: '/search?q=:query',
  AI_ASK: '/ai/ask',
  RECOMMENDATIONS: '/ai/recommendations',
  SETTINGS: '/settings',
  SETTINGS_PROFILE: '/settings/profile',
  SETTINGS_SECURITY: '/settings/security',
  SETTINGS_PREFERENCES: '/settings/preferences',
} as const;
```

## Route Tree

```typescript
// src/routes/index.tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { DashboardLayout } from '@/layouts/DashboardLayout';
import { AuthLayout } from '@/layouts/AuthLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { PublicRoute } from './PublicRoute';
import { ROUTES } from './routePaths';

export const router = createBrowserRouter([
  // ============================================
  // Public Routes (guest only)
  // ============================================
  {
    element: <PublicRoute />,
    children: [
      {
        element: <AuthLayout />,
        children: [
          {
            path: ROUTES.LOGIN,
            lazy: () => import('@/pages/auth/LoginPage'),
          },
          {
            path: ROUTES.REGISTER,
            lazy: () => import('@/pages/auth/RegisterPage'),
          },
          {
            path: ROUTES.FORGOT_PASSWORD,
            lazy: () => import('@/pages/auth/ForgotPasswordPage'),
          },
        ],
      },
    ],
  },

  // ============================================
  // Protected Routes (authenticated only)
  // ============================================
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          // Dashboard
          {
            path: ROUTES.DASHBOARD,
            lazy: () => import('@/pages/dashboard/DashboardPage'),
          },

          // Containers
          {
            path: ROUTES.CONTAINERS,
            lazy: () => import('@/pages/containers/ContainerListPage'),
          },
          {
            path: ROUTES.CONTAINER_CREATE,
            lazy: () => import('@/pages/containers/ContainerCreatePage'),
          },
          {
            path: ROUTES.CONTAINER_DETAIL,
            lazy: () => import('@/pages/containers/ContainerDetailPage'),
          },
          {
            path: ROUTES.CONTAINER_EDIT,
            lazy: () => import('@/pages/containers/ContainerEditPage'),
          },

          // Tags
          {
            path: ROUTES.TAGS,
            lazy: () => import('@/pages/tags/TagsPage'),
          },

          // Search
          {
            path: ROUTES.SEARCH,
            lazy: () => import('@/pages/search/SearchPage'),
          },

          // AI
          {
            path: ROUTES.AI_ASK,
            lazy: () => import('@/pages/ai/AIAskPage'),
          },
          {
            path: ROUTES.RECOMMENDATIONS,
            lazy: () => import('@/pages/ai/RecommendationsPage'),
          },

          // Settings
          {
            path: ROUTES.SETTINGS,
            lazy: () => import('@/pages/settings/SettingsPage'),
          },
        ],
      },
    ],
  },

  // ============================================
  // Fallback
  // ============================================
  {
    path: '*',
    lazy: () => import('@/pages/NotFoundPage'),
  },
]);
```

## Route Guards

```typescript
// src/routes/ProtectedRoute.tsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from './routePaths';

export function ProtectedRoute() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    // Redirect to login with return URL
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
}

// src/routes/PublicRoute.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from './routePaths';

export function PublicRoute() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (isAuthenticated) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <Outlet />;
}
```

## Navigation Structure

```typescript
// src/layouts/components/Sidebar.tsx
interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
  badge?: number;
  children?: NavItem[];
}

const navItems: NavItem[] = [
  {
    label: 'Dashboard',
    path: ROUTES.DASHBOARD,
    icon: LayoutDashboard,
  },
  {
    label: 'Containers',
    path: ROUTES.CONTAINERS,
    icon: Package,
    children: [
      { label: 'All', path: ROUTES.CONTAINERS, icon: List },
      { label: 'Books', path: `${ROUTES.CONTAINERS}?type=BOOK`, icon: Book },
      { label: 'Movies', path: `${ROUTES.CONTAINERS}?type=MOVIE`, icon: Film },
      { label: 'Projects', path: `${ROUTES.CONTAINERS}?type=SOFTWARE_PROJECT`, icon: Code },
      { label: 'Goals', path: `${ROUTES.CONTAINERS}?type=GOAL`, icon: Target },
      { label: 'Habits', path: `${ROUTES.CONTAINERS}?type=HABIT`, icon: CheckSquare },
    ],
  },
  {
    label: 'Search',
    path: ROUTES.SEARCH,
    icon: Search,
  },
  {
    label: 'AI',
    path: ROUTES.AI_ASK,
    icon: Brain,
    children: [
      { label: 'Ask AI', path: ROUTES.AI_ASK, icon: MessageSquare },
      { label: 'Recommendations', path: ROUTES.RECOMMENDATIONS, icon: Sparkles },
    ],
  },
  {
    label: 'Tags',
    path: ROUTES.TAGS,
    icon: Tags,
  },
  {
    label: 'Settings',
    path: ROUTES.SETTINGS,
    icon: Settings,
  },
];
```

## URL Parameters & Query Strings

```typescript
// Container list with filters in URL
// /containers?type=BOOK&status=ACTIVE&tags=psychology&sortBy=createdAt&sortDir=desc

// Search query in URL
// /search?q=cognitive+bias&type=HYBRID&types=BOOK,KNOWLEDGE_ASSET

// Container detail
// /containers/{containerId}

// Edit container
// /containers/{containerId}/edit

// Tab state in URL
// /containers/{containerId}?tab=timeline
// /containers/{containerId}?tab=ai-context

// Pagination
// /containers?page=2&size=20
```

## Lazy Loading

All page components use React.lazy for code splitting:

```typescript
// Each page is a separate chunk
const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage'));
const ContainerListPage = lazy(() => import('@/pages/containers/ContainerListPage'));
// ...

// Suspense boundary in layout
function DashboardLayout() {
  return (
    <div className="flex h-screen">
      <Sidebar />
      <main className="flex-1 overflow-auto">
        <Suspense fallback={<PageSkeleton />}>
          <Outlet />
        </Suspense>
      </main>
    </div>
  );
}
```
