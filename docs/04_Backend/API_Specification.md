# API Specification

## API Overview

- **Base URL**: `/api/v1`
- **Protocol**: HTTPS
- **Format**: JSON
- **Auth**: Bearer JWT
- **Versioning**: URL path versioning (`/api/v1/...`)
- **Documentation**: OpenAPI 3.0 at `/api/swagger-ui.html`

## Standard Response Envelope

```json
// Success
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-01-15T10:30:00Z"
}

// Error
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "title", "message": "Title is required" }
    ]
  },
  "timestamp": "2026-01-15T10:30:00Z"
}

// Paginated
{
  "success": true,
  "data": [ ... ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

## Authentication Endpoints

### POST /api/v1/auth/register
Register a new user account.

```json
// Request
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "displayName": "Alex User"
}

// Validation Rules
// email: required, valid email format, max 255 chars
// password: required, min 8 chars, must contain uppercase + lowercase + number + special
// displayName: required, min 2 chars, max 100 chars

// Response 201
{
  "success": true,
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "displayName": "Alex User",
    "accessToken": "eyJhbG...",
    "refreshToken": "dGhpcyBp...",
    "expiresIn": 900000
  }
}
```

### POST /api/v1/auth/login
Authenticate with email and password.

```json
// Request
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

// Response 200
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "dGhpcyBp...",
    "tokenType": "Bearer",
    "expiresIn": 900000
  }
}

// Response 401
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password"
  }
}
```

### POST /api/v1/auth/refresh
Refresh an expired access token.

```json
// Request
{
  "refreshToken": "dGhpcyBp..."
}

// Response 200
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "expiresIn": 900000
  }
}
```

### POST /api/v1/auth/logout
Invalidate the current session.

```json
// Headers: Authorization: Bearer <token>
// Request: { "refreshToken": "dGhpcyBp..." }
// Response 200: { "success": true, "data": { "message": "Logged out successfully" } }
```

## Container Endpoints

### GET /api/v1/containers
List containers with filtering and pagination.

```json
// Query Parameters
// type: BOOK | MOVIE | TV_SERIES | COURSE | LEARNING_PROGRESS | SOFTWARE_PROJECT | GOAL | HABIT | NOTE | SNAPSHOT | PINNED_CONTENT | KNOWLEDGE_ASSET
// status: DRAFT | ACTIVE | COMPLETED | ARCHIVED
// search: string (full-text search)
// tags: comma-separated tag names
// sortBy: title | createdAt | updatedAt | status | progressPercentage
// sortDir: asc | desc
// page: integer (0-based, default 0)
// size: integer (default 20, max 100)

// Response 200
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "type": "BOOK",
      "title": "Thinking, Fast and Slow",
      "description": "A book about cognitive biases...",
      "status": "ACTIVE",
      "progressPercentage": 50,
      "tags": ["psychology", "decision-making"],
      "metadata": {
        "author": "Daniel Kahneman",
        "isbn": "9780143127741"
      },
      "aiContext": {
        "summary": "Explores the two systems of thinking...",
        "autoTags": ["cognitive science", "behavioral economics"],
        "enrichmentStatus": "COMPLETED"
      },
      "createdAt": "2026-01-10T08:00:00Z",
      "updatedAt": "2026-01-15T10:30:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

### POST /api/v1/containers
Create a new container.

```json
// Request
{
  "type": "BOOK",
  "title": "Thinking, Fast and Slow",
  "description": "A groundbreaking tour of the mind...",
  "metadata": {
    "author": "Daniel Kahneman",
    "isbn": "9780143127741",
    "pageCount": 499,
    "genre": "Psychology",
    "readingStatus": "READING"
  },
  "tags": ["psychology", "decision-making"],
  "initialProgress": 0
}

// Validation Rules
// type: required, must be valid ContainerType
// title: required, 1-500 chars
// description: optional, max 10000 chars
// metadata: type-specific validation
// tags: optional, max 20 tags, each 1-100 chars
// initialProgress: optional, 0-100

// Response 201
{
  "success": true,
  "data": {
    "id": "uuid",
    "type": "BOOK",
    "title": "Thinking, Fast and Slow",
    "status": "ACTIVE",
    "progressPercentage": 0,
    "createdAt": "2026-01-15T10:30:00Z"
  }
}

// Response 400
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "metadata.isbn", "message": "ISBN must be 10 or 13 digits" }
    ]
  }
}
```

### GET /api/v1/containers/{id}
Get a container by ID with full details.

```json
// Response 200
{
  "success": true,
  "data": {
    "id": "uuid",
    "type": "BOOK",
    "title": "Thinking, Fast and Slow",
    "description": "...",
    "status": "ACTIVE",
    "progressPercentage": 50,
    "progressData": {
      "currentPage": 250,
      "totalPages": 499,
      "startedAt": "2026-01-10",
      "estimatedCompletion": "2026-02-15"
    },
    "metadata": {
      "author": "Daniel Kahneman",
      "isbn": "9780143127741",
      "pageCount": 499,
      "currentPage": 250,
      "genre": "Psychology",
      "publisher": "Farrar, Straus and Giroux",
      "publishedYear": 2011,
      "readingStatus": "READING"
    },
    "tags": [
      { "id": "uuid", "name": "psychology", "color": "#6366f1" },
      { "id": "uuid", "name": "decision-making", "color": "#10b981" }
    ],
    "aiContext": {
      "summary": "Explores the two systems that drive the way we think...",
      "summaryModel": "mistral:7b-q4_K_M",
      "summaryGeneratedAt": "2026-01-10T08:05:00Z",
      "autoTags": ["cognitive science", "behavioral economics", "heuristics"],
      "autoTagScores": [0.95, 0.88, 0.82],
      "enrichmentStatus": "COMPLETED",
      "lastEnrichedAt": "2026-01-10T08:05:00Z"
    },
    "timeline": {
      "total": 5,
      "recent": [
        { "type": "PROGRESS_UPDATED", "description": "Progress updated to 50%", "createdAt": "..." },
        { "type": "CREATED", "description": "Container created", "createdAt": "..." }
      ]
    },
    "snapshotCount": 2,
    "isPinned": true,
    "createdAt": "2026-01-10T08:00:00Z",
    "updatedAt": "2026-01-15T10:30:00Z"
  }
}
```

### PUT /api/v1/containers/{id}
Update a container.

```json
// Request
{
  "title": "Thinking, Fast and Slow (Updated)",
  "description": "Updated description...",
  "metadata": {
    "currentPage": 300
  },
  "tags": ["psychology", "decision-making", "behavioral-economics"],
  "status": "ACTIVE"
}

// Response 200: Updated container
```

### PATCH /api/v1/containers/{id}/progress
Update progress for a container.

```json
// Request
{
  "progress": 75,
  "note": "Finished Part 2 about heuristics and biases"
}

// Response 200
{
  "success": true,
  "data": {
    "progressPercentage": 75,
    "timelineEvent": {
      "type": "PROGRESS_UPDATED",
      "description": "Progress updated to 75%"
    }
  }
}
```

### DELETE /api/v1/containers/{id}
Soft-delete a container.

```json
// Response 204: No Content
```

### POST /api/v1/containers/{id}/restore
Restore a soft-deleted container.

```json
// Response 200: Restored container
```

## Tag Endpoints

### GET /api/v1/tags
List all tags for the current user.

```json
// Response 200
{
  "success": true,
  "data": [
    { "id": "uuid", "name": "psychology", "color": "#6366f1", "containerCount": 5 },
    { "id": "uuid", "name": "decision-making", "color": "#10b981", "containerCount": 3 }
  ]
}
```

### POST /api/v1/tags
Create a new tag.

```json
// Request
{
  "name": "machine-learning",
  "color": "#f59e0b",
  "description": "ML and AI related content"
}

// Validation: name required, 1-100 chars, unique per user, color optional hex
```

### PUT /api/v1/tags/{id}
Update a tag.

### DELETE /api/v1/tags/{id}
Delete a tag and remove from all containers.

## Snapshot Endpoints

### POST /api/v1/containers/{containerId}/snapshots
Create a snapshot of a container's current state.

```json
// Request
{
  "label": "Before major refactor"
}

// Response 201
{
  "success": true,
  "data": {
    "id": "uuid",
    "version": 3,
    "label": "Before major refactor",
    "createdAt": "2026-01-15T10:30:00Z"
  }
}
```

### GET /api/v1/containers/{containerId}/snapshots
List all snapshots for a container.

### GET /api/v1/containers/{containerId}/snapshots/{snapshotId}
Get snapshot details.

### GET /api/v1/containers/{containerId}/snapshots/{snapshotId}/diff/{otherSnapshotId}
Compare two snapshots.

## Timeline Endpoints

### GET /api/v1/containers/{containerId}/timeline
Get timeline events for a container.

```json
// Query Parameters
// types: comma-separated event types
// from: ISO date
// to: ISO date
// page, size

// Response 200
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "type": "PROGRESS_UPDATED",
      "description": "Progress updated to 50%",
      "oldValue": "25",
      "newValue": "50",
      "createdAt": "2026-01-15T10:30:00Z"
    }
  ]
}
```

## Pin Endpoints

### POST /api/v1/pins
Pin a container.

```json
// Request: { "containerId": "uuid" }
// Response 201
```

### DELETE /api/v1/pins/{containerId}
Unpin a container.

### GET /api/v1/pins
Get all pinned containers.

## Search Endpoints

### POST /api/v1/search
Search across all containers (hybrid search in V2+).

```json
// Request
{
  "query": "books about cognitive biases and decision making",
  "type": "HYBRID",
  "filters": {
    "containerTypes": ["BOOK", "KNOWLEDGE_ASSET"],
    "status": ["ACTIVE", "COMPLETED"],
    "tags": ["psychology"],
    "dateFrom": "2026-01-01",
    "dateTo": "2026-12-31"
  },
  "limit": 20,
  "offset": 0
}

// Response 200
{
  "success": true,
  "data": [
    {
      "containerId": "uuid",
      "type": "BOOK",
      "title": "Thinking, Fast and Slow",
      "relevance": 0.95,
      "matchedField": "description",
      "snippet": "...two systems of thinking: System 1 is fast and intuitive, System 2 is slow and deliberate...",
      "semanticScore": 0.92,
      "keywordScore": 0.78
    }
  ],
  "total": 15,
  "searchType": "HYBRID",
  "executionMs": 245
}
```

## AI Endpoints

### POST /api/v1/ai/ask
Ask a question about your knowledge base (RAG).

```json
// Request
{
  "question": "What have I learned about cognitive biases?",
  "maxTokens": 500,
  "temperature": 0.3,
  "includeSources": true
}

// Response 200 (streaming via WebSocket)
{
  "success": true,
  "data": {
    "answer": "Based on your knowledge base, you've explored several cognitive biases...",
    "sources": [
      {
        "containerId": "uuid",
        "title": "Thinking, Fast and Slow",
        "type": "BOOK",
        "relevance": 0.94,
        "excerpt": "System 1 operates automatically and quickly..."
      }
    ],
    "confidence": 0.87,
    "modelUsed": "mistral:7b-q4_K_M"
  }
}
```

### POST /api/v1/ai/enrich/{containerId}
Request AI enrichment for a specific container.

```json
// Response 202
{
  "success": true,
  "data": {
    "message": "Enrichment started",
    "enrichmentId": "uuid",
    "status": "PROCESSING"
  }
}
```

### GET /api/v1/ai/recommendations
Get AI-powered recommendations.

```json
// Query Parameters
// containerId: uuid (optional, get recommendations based on specific container)
// limit: integer (default 10)

// Response 200
{
  "success": true,
  "data": [
    {
      "container": { "id": "uuid", "title": "...", "type": "BOOK" },
      "score": 0.92,
      "reason": "Similar to your current reading on cognitive science"
    }
  ]
}
```

## WebSocket Endpoints

### STOMP Topics

```
Subscribe:
  /user/queue/notifications     - User-specific notifications
  /user/queue/enrichment        - AI enrichment progress
  /topic/containers/{id}        - Container-specific updates
  /topic/activity               - Global activity feed (admin)

Send:
  /app/container.update         - Broadcast container update
  /app/presence                 - User presence heartbeat
```

### WebSocket Message Format

```json
{
  "type": "CONTAINER_UPDATED",
  "containerId": "uuid",
  "userId": "uuid",
  "timestamp": "2026-01-15T10:30:00Z",
  "payload": {
    "field": "progressPercentage",
    "oldValue": 25,
    "newValue": 50
  }
}
```

## API Versioning

```yaml
Versioning Strategy:
  method: URL path
  format: /api/{version}/
  
  versions:
    v1: Initial release (2026-Q2)
    v2: AI features, enhanced search (2026-Q3)
    
  deprecation:
    - Old versions supported for 6 months after new version
    - Deprecation header: Sunset: Sat, 01 Jan 2027 00:00:00 GMT
    - Migration guide provided 3 months before deprecation
  
  breaking changes:
    - Only in major version bumps
    - Non-breaking additions allowed in current version
    - Fields can be added to responses (ignore unknown fields)
```

## Rate Limiting

```yaml
Rate Limits:
  general:
    limit: 100 requests/minute
    burst: 150
    headers: X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset
  
  auth:
    login: 5 attempts/minute per IP
    register: 3 attempts/hour per IP
  
  ai:
    enrich: 10 requests/minute per user
    ask: 20 requests/minute per user
  
  search:
    search: 60 requests/minute per user
```
