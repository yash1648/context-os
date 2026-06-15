# Design System

## Brand Identity

```yaml
Brand:
  name: ContextOS
  tagline: Your Intelligent Context Layer
  colors:
    primary: '#6366f1'    # Indigo - trust, intelligence
    secondary: '#8b5cf6'  # Violet - creativity, depth
    accent: '#06b6d4'     # Cyan - clarity, focus
    success: '#10b981'    # Emerald - progress, growth
    warning: '#f59e0b'    # Amber - attention, reminders
    error: '#ef4444'      # Red - alerts, errors
```

## Color Palette

```typescript
// tailwind.config.ts
export default {
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',  // Primary
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
          950: '#1e1b4b',
        },
        surface: {
          DEFAULT: '#ffffff',
          secondary: '#f8fafc',
          tertiary: '#f1f5f9',
          hover: '#e2e8f0',
          // Dark mode
          dark: '#0f172a',
          'dark-secondary': '#1e293b',
          'dark-tertiary': '#334155',
          'dark-hover': '#475569',
        },
      },
    },
  },
};
```

## Typography

```css
/* Font scale */
--font-sans: 'Inter', system-ui, -apple-system, sans-serif;
--font-mono: 'JetBrains Mono', 'Fira Code', monospace;

/* Font sizes */
--text-xs: 0.75rem;     /* 12px - Labels, metadata */
--text-sm: 0.875rem;    /* 14px - Body small, descriptions */
--text-base: 1rem;      /* 16px - Body text */
--text-lg: 1.125rem;    /* 18px - Large body */
--text-xl: 1.25rem;     /* 20px - Subheadings */
--text-2xl: 1.5rem;     /* 24px - Section headings */
--text-3xl: 1.875rem;   /* 30px - Page titles */
--text-4xl: 2.25rem;    /* 36px - Hero titles */

/* Font weights */
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;

/* Line heights */
--leading-tight: 1.25;
--leading-normal: 1.5;
--leading-relaxed: 1.625;
```

## Spacing

```css
/* 4px base unit */
--space-1: 0.25rem;   /* 4px */
--space-2: 0.5rem;    /* 8px */
--space-3: 0.75rem;   /* 12px */
--space-4: 1rem;      /* 16px */
--space-5: 1.25rem;   /* 20px */
--space-6: 1.5rem;    /* 24px */
--space-8: 2rem;      /* 32px */
--space-10: 2.5rem;   /* 40px */
--space-12: 3rem;     /* 48px */
--space-16: 4rem;     /* 64px */
--space-20: 5rem;     /* 80px */
--space-24: 6rem;     /* 96px */
```

## Shadows

```css
--shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
--shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
--shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
--shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
--shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
```

## UI Component Specifications

### Button

```typescript
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'ghost' | 'danger' | 'outline';
  size: 'sm' | 'md' | 'lg';
  loading?: boolean;
  disabled?: boolean;
  icon?: LucideIcon;
  iconPosition?: 'left' | 'right';
  fullWidth?: boolean;
}

// Variants
// primary:   bg-brand-500 text-white hover:bg-brand-600
// secondary: bg-surface-secondary text-foreground hover:bg-surface-tertiary
// ghost:     bg-transparent text-foreground hover:bg-surface-tertiary
// danger:    bg-red-500 text-white hover:bg-red-600
// outline:   border border-border bg-transparent hover:bg-surface-secondary

// Sizes
// sm: px-3 py-1.5 text-sm
// md: px-4 py-2 text-sm
// lg: px-6 py-3 text-base

// States
// loading: show spinner, disable interaction
// disabled: opacity-50, cursor-not-allowed
```

### Card

```typescript
interface CardProps {
  variant?: 'default' | 'interactive' | 'highlighted';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hover?: boolean;
}

// Structured layout:
// ┌─────────────────────────┐
// │ CardHeader (optional)   │
// │   Title, action button  │
// ├─────────────────────────┤
// │ CardContent              │
// │   Main content area     │
// ├─────────────────────────┤
// │ CardFooter (optional)   │
// │   Actions, metadata     │
// └─────────────────────────┘
```

### Form Elements

```typescript
// Input
interface InputProps {
  label?: string;
  helperText?: string;
  error?: string;
  leftIcon?: LucideIcon;
  rightIcon?: LucideIcon;
  size?: 'sm' | 'md' | 'lg';
}

// Select
interface SelectProps {
  label?: string;
  options: { value: string; label: string }[];
  placeholder?: string;
  error?: string;
}

// Tag Input
interface TagInputProps {
  tags: string[];
  onChange: (tags: string[]) => void;
  suggestions?: string[];
  maxTags?: number;
}
```

### Progress Indicators

```typescript
// Progress Bar
interface ProgressBarProps {
  value: number;        // 0-100
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'success' | 'warning' | 'danger';
  showLabel?: boolean;
  animated?: boolean;
}

// Skeleton
interface SkeletonProps {
  variant: 'text' | 'circular' | 'rectangular' | 'card';
  width?: string | number;
  height?: string | number;
  count?: number;  // Repeated skeleton lines
}

// Loading Spinner
interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'brand';
}
```

## Dark Mode

```css
/* Light mode (default) */
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f8fafc;
  --bg-tertiary: #f1f5f9;
  --text-primary: #0f172a;
  --text-secondary: #475569;
  --text-tertiary: #94a3b8;
  --border: #e2e8f0;
}

/* Dark mode */
.dark {
  --bg-primary: #0f172a;
  --bg-secondary: #1e293b;
  --bg-tertiary: #334155;
  --text-primary: #f1f5f9;
  --text-secondary: #94a3b8;
  --text-tertiary: #64748b;
  --border: #334155;
}
```

## Container Type Icons & Colors

```typescript
const containerTypeConfig: Record<ContainerType, {
  icon: LucideIcon;
  color: string;
  label: string;
}> = {
  BOOK:          { icon: BookOpen,     color: '#3b82f6',  label: 'Book' },
  MOVIE:         { icon: Film,         color: '#8b5cf6',  label: 'Movie' },
  TV_SERIES:     { icon: Tv,           color: '#ec4899',  label: 'TV Series' },
  COURSE:        { icon: GraduationCap, color: '#10b981', label: 'Course' },
  LEARNING_PROGRESS: { icon: Brain,    color: '#14b8a6',  label: 'Learning' },
  SOFTWARE_PROJECT: { icon: Code,      color: '#f59e0b',  label: 'Project' },
  GOAL:          { icon: Target,       color: '#ef4444',  label: 'Goal' },
  HABIT:         { icon: CheckSquare,  color: '#84cc16',  label: 'Habit' },
  NOTE:          { icon: StickyNote,   color: '#6366f1',  label: 'Note' },
  SNAPSHOT:      { icon: Camera,       color: '#a855f7',  label: 'Snapshot' },
  PINNED_CONTENT: { icon: Bookmark,    color: '#f43f5e',  label: 'Pinned' },
  KNOWLEDGE_ASSET: { icon: Gem,        color: '#06b6d4',  label: 'Asset' },
};
```

## Animation Tokens

```css
/* Duration */
--duration-fast: 150ms;
--duration-normal: 200ms;
--duration-slow: 300ms;

/* Easing */
--ease-out: cubic-bezier(0.16, 1, 0.3, 1);
--ease-in-out: cubic-bezier(0.65, 0, 0.35, 1);
--ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);

/* Usage */
--transition-fast: 150ms var(--ease-out);
--transition-normal: 200ms var(--ease-out);
--transition-spring: 300ms var(--ease-spring);
```

## Tailwind Utility Classes

```css
/* Custom utilities used across the app */
.focus-ring {
  @apply focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2;
}

.card-hover {
  @apply transition-shadow duration-200 hover:shadow-md;
}

.text-balance {
  text-wrap: balance;
}

.gradient-brand {
  @apply bg-gradient-to-br from-brand-500 to-violet-600;
}
```
