# User Stories

## Epics & Stories

### Epic: Container Management

**Story 1: Create a Book Container**
> As a **reader**, I want to **create a book container with metadata** so that I can **track my reading progress and remember key details**.

**Acceptance Criteria:**
- Form with fields: Title, Author, ISBN, Genre, Page Count, Status (Want to Read / Reading / Completed)
- Auto-suggest book metadata from ISBN
- Tags and notes fields
- Container appears in dashboard immediately
- Confirmation toast on success

**Story 2: Track Reading Progress**
> As a **reader**, I want to **update my progress on a book** so that I can **know exactly where I left off**.

**Acceptance Criteria:**
- Update page number / percentage
- Optionally add notes about current section
- Timeline event recorded for progress update
- Estimated completion time recalculated

**Story 3: Save a Knowledge Asset**
> As a **lifelong learner**, I want to **save code snippets, quotes, and insights as Knowledge Assets** so that I can **retrieve them later**.

**Acceptance Criteria:**
- Rich text editor for asset content
- Source attribution (book, article, course, conversation)
- Tags for categorization
- Link to parent container
- Full-text searchable

**Story 4: Create a Goal with Key Results**
> As a **goal-oriented user**, I want to **create goals with measurable key results** so that I can **track progress toward my objectives**.

**Acceptance Criteria:**
- Goal title, description, deadline
- 2-5 key results with numeric targets
- Progress automatically calculated from key results
- Reminders at configurable intervals
- Timeline of goal progress

**Story 5: Log a Habit**
> As a **self-improvement enthusiast**, I want to **log daily habits** so that I can **build consistency and see streaks**.

**Acceptance Criteria:**
- Configure frequency (daily, weekly, custom)
- One-click log completion
- Streak counter with visual indicator
- Calendar view of habit history
- Reminder notifications

### Epic: Search & Discovery

**Story 6: Search Across All Containers**
> As a **power user**, I want to **search across all my containers** so that I can **find anything quickly**.

**Acceptance Criteria:**
- Global search bar accessible from any page
- Searches titles, descriptions, tags, notes content
- Filter by container type
- Sort by relevance, date, title
- Keyboard shortcut (Cmd+K / Ctrl+K)

**Story 7: Semantic Search**
> As a **knowledge worker**, I want to **search by meaning, not just keywords** so that I can **find things even when I don't remember exact terms**.

**Acceptance Criteria:**
- Natural language queries ("books about distributed systems")
- Results ranked by semantic relevance
- Highlight matching passages
- Hybrid search with keyword fallback

**Story 8: Discover Related Containers**
> As a **curious learner**, I want to **see containers related to what I'm viewing** so that I can **discover connections in my knowledge**.

**Acceptance Criteria:**
- Related containers panel on detail pages
- Relationship explained ("Similar to", "Referenced in", "Prerequisite for")
- Click to navigate to related container
- Based on embeddings + tags + links

### Epic: AI Enrichment

**Story 9: Auto-Generate Summary**
> As a **busy user**, I want **AI to generate summaries of my containers** so that I can **quickly recall what something is about**.

**Acceptance Criteria:**
- Summary generated when container is created
- Summary updates when content changes
- Summary shown in list and detail views
- Option to regenerate or edit summary
- Model and prompt configurable

**Story 10: Get AI-Powered Answers**
> As a **researcher**, I want to **ask questions about my knowledge base** so that I can **get answers synthesized from my containers**.

**Acceptance Criteria:**
- Natural language question input
- Answer with citations to source containers
- Confidence score for answer
- Follow-up questions supported
- Answers cached for repeated queries

### Epic: Real-Time Updates

**Story 11: See Changes Instantly**
> As a **multi-device user**, I want **changes I make on one device to show up instantly on another** so that I can **seamlessly switch between devices**.

**Acceptance Criteria:**
- Changes propagate in < 1 second
- No page refresh required
- Conflict indicator if simultaneous edits occur
- Offline queue with auto-sync

### Epic: Browser Extension

**Story 12: Save a Web Article**
> As a **researcher**, I want to **save a web article to ContextOS from my browser** so that I can **capture information without breaking my flow**.

**Acceptance Criteria:**
- Click extension icon to save current page
- Auto-detect page type (article, documentation, video)
- Extract title, description, metadata
- Choose or create container on save
- Toast notification on success

**Story 13: Capture a Highlight**
> As a **student**, I want to **highlight text on a web page and save it directly to a container** so that I can **build my knowledge base while reading**.

**Acceptance Criteria:**
- Select text, right-click, "Save to ContextOS"
- Context menu option for selected text
- Auto-link to source URL and page title
- Choose destination container
- Tags editable before save

### Epic: VSCode Integration

**Story 14: Track Project Context**
> As a **developer**, I want to **auto-track my current project as a container** so that I can **maintain context on what I'm building**.

**Acceptance Criteria:**
- VSCode extension detects project directory
- Auto-populates tech stack from package.json, pom.xml, etc.
- Links to git repository
- Updates on commit

**Story 15: Save Code Snippets**
> As a **developer**, I want to **save code snippets with context** so that I can **reuse patterns and remember why I chose them**.

**Acceptance Criteria:**
- Select code in editor, command to save
- Captures code, language, file path, project
- Adds to Knowledge Assets
- Syntax highlighting in view

### Epic: Personal OS

**Story 16: Daily Context Brief**
> As a **power user**, I want a **daily briefing of relevant items** so that I can **start my day informed**.

**Acceptance Criteria:**
- Morning notification with digest
- Includes: items due soon, suggested reading, goal progress, recently added
- AI-curated based on user's priorities
- Configurable content and timing

**Story 17: Proactive Suggestions**
> As a **lifelong learner**, I want **ContextOS to suggest what to read, watch, or learn next** so that I can **continuously grow in areas I care about**.

**Acceptance Criteria:**
- Based on current containers, goals, and learning history
- Considers available time (quick read vs. deep course)
- Updates weekly with new recommendations
- One-click to add recommended item as container
