# Component Design

## Component Hierarchy

```
App
├── AuthLayout
│   ├── LoginPage
│   ├── RegisterPage
│   └── ForgotPasswordPage
│
└── DashboardLayout
    ├── Sidebar
    │   ├── Logo
    │   ├── NavLinks (Dashboard, Containers, Search, AI, Tags, Settings)
    │   ├── QuickActions (New Container, New Tag)
    │   └── UserMenu (Profile, Settings, Logout)
    │
    ├── Header
    │   ├── SearchBar (global Cmd+K search)
    │   ├── NotificationBell
    │   ├── UserAvatar
    │   └── MobileMenuToggle
    │
    └── MainContent (routes)
        ├── DashboardPage
        │   ├── StatsCard (×4: Active, Completed, Pinned, Streak)
        │   ├── RecentContainers
        │   ├── ActivityFeed
        │   └── ProgressSummary
        │
        ├── ContainerListPage
        │   ├── PageHeader (title + NewContainer button)
        │   ├── SearchFilters (type, status, tags, date range)
        │   ├── ContainerGrid / ContainerList (toggle)
        │   └── Pagination
        │
        ├── ContainerDetailPage
        │   ├── PageHeader (title, type badge, actions)
        │   ├── Tabs
        │   │   ├── Overview
        │   │   │   ├── Description
        │   │   │   ├── MetadataPanel
        │   │   │   ├── ProgressWidget
        │   │   │   └── TagsList
        │   │   ├── Timeline
        │   │   │   └── TimelineEventList
        │   │   ├── Snapshots
        │   │   │   ├── SnapshotList
        │   │   │   └── SnapshotCompare
        │   │   ├── AI Context
        │   │   │   ├── SummarySection
        │   │   │   ├── AutoTagsSection
        │   │   │   ├── RelatedContainers
        │   │   │   └── EnrichmentStatus
        │   │   └── Activity
        │   │       └── ActivityFeed
        │   └── SidePanel (related, pinned status)
        │
        ├── SearchPage
        │   ├── SearchInput
        │   ├── FilterPanel (type, tags, status, date)
        │   ├── SearchResults
        │   └── SearchPagination
        │
        ├── AIAskPage
        │   ├── AIQuestionInput
        │   ├── AIAnswerDisplay (streaming)
        │   └── SourceCitations
        │
        └── SettingsPage
            ├── ProfileSection
            ├── PreferencesSection
            └── SecuritySection
```

## Key Component Specifications

### ContainerCard

```typescript
interface ContainerCardProps {
  container: ContainerSummary;
  onPin?: (id: string) => void;
  onArchive?: (id: string) => void;
  variant?: 'grid' | 'list';
}

// Grid variant - compact card with key info
// List variant - full row with more details

// Features:
// - Type icon (book, movie, project, etc.)
// - Title, description (truncated)
// - Progress bar
// - Status badge
// - Tags (max 3, overflow "+N")
// - Pin toggle
// - Click to navigate to detail
// - Hover: subtle elevation, quick actions
// - Skeleton loading state
// - Empty state (no containers)
```

### ContainerForm

```typescript
interface ContainerFormProps {
  mode: 'create' | 'edit';
  type?: ContainerType; // Required for create
  initialData?: ContainerDetail;
  onSubmit: (data: ContainerFormData) => void;
  onCancel: () => void;
}

// Dynamic form that changes based on container type:
// BOOK: ISBN, Author, Pages, Genre, Reading Status
// MOVIE: Director, Year, Duration, Genre, Watch Status
// GOAL: Objective, Key Results, Deadline, Category
// etc.

// Features:
// - Type selector (create mode only)
// - Dynamic field rendering based on type
// - Tag input with autocomplete
// - Rich text for description
// - JSON metadata editor for advanced users
// - Auto-save draft to localStorage
// - Validation errors inline
// - Submit with loading state
```

### SearchBar (Command Palette)

```typescript
interface SearchBarProps {
  onSelect?: (result: SearchResult) => void;
  placeholder?: string;
}

// Global Cmd+K / Ctrl+K search accessible from anywhere

// Features:
// - Modal overlay (cmdk library)
// - Recent searches at top
// - Keyboard navigation (arrows, enter, escape)
// - Filter by type: "book:neuromancer"
// - Filter by tag: "tag:psychology"
// - Search across: title, description, tags, notes
// - Results grouped by type
// - Highlight matching text
// - Quick actions: "Create new book", "Go to settings"
// - Debounced input (300ms)
```

### AIAnswerDisplay

```typescript
interface AIAnswerDisplayProps {
  answer: string;
  sources: SourceCitation[];
  isStreaming: boolean;
  confidence: number;
  onSourceClick: (containerId: string) => void;
  onFollowUp: (question: string) => void;
}

// Streaming answer display with source citations

// Features:
// - Markdown rendering of answer
// - Streaming text animation
// - Source citations as clickable cards
// - Confidence indicator
// - Follow-up question suggestions
// - Copy answer button
// - Regenerate button
// - Feedback buttons (helpful/not helpful)
```

## Responsive Breakpoints

```typescript
const breakpoints = {
  sm: 640,   // Mobile landscape
  md: 768,   // Tablet
  lg: 1024,  // Desktop
  xl: 1280,  // Large desktop
  '2xl': 1536, // Extra large
};

// Layout changes:
// Mobile (< 768px): Bottom nav, single column, full-width modals
// Tablet (768-1024px): Sidebar collapsible, 2-column grid
// Desktop (1024+): Fixed sidebar, 3-column grid, side panels
```

## Accessibility Standards

```typescript
// All components follow WCAG 2.1 AA standards:
// - Keyboard navigable
// - Focus indicators
// - Screen reader labels
// - Color contrast ratio >= 4.5:1
// - Aria attributes on dynamic content
// - Reduced motion support
// - Focus trap in modals
// - Skip to content link
```

## Error States

Each data-fetching component handles:

```typescript
// 1. Loading state: Skeleton components
// 2. Empty state: Illustration + message + CTA
// 3. Error state: Error message + retry button
// 4. Offline state: Banner + cached data display
// 5. Partial data: Show available data + loading indicators for missing
```
