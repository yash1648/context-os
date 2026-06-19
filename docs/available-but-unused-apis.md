# Available Backend APIs — Not Yet Wired in Frontend

These endpoints exist in the backend but are not currently called from any frontend page or component. They're ready to use — just need UI integration.

---

## Containers

### `POST /api/v1/containers/{id}/restore` — Restore soft-deleted container

Restores a container that was previously soft-deleted (sets `deletedAt` back to `null`).

**Auth:** JWT required  
**Response:** `ApiResponse<ContainerResponse>`

```typescript
// Frontend API already has the function:
containersApi.restore(id: string)
// Currently not called from any page
```

**Use case:** An "undo" action after deleting a container, or a "trash can" view with restore functionality.

---

### `DELETE /api/v1/containers/{id}/hard` — Permanently delete container

**⚠️ Requires ADMIN role** (`@PreAuthorize("hasRole('ADMIN')")`). Permanently removes a soft-deleted container from the database. Does not work on active (non-deleted) containers.

**Auth:** JWT required + ADMIN role  
**Response:** `ApiResponse<void>`

```typescript
// No frontend function exists yet
// You'd need to add: apiClient.delete(`/api/v1/containers/${id}/hard`)
```

**Use case:** Admin panel to permanently purge deleted containers.

---

## Snapshots

### `GET /api/v1/snapshots/{id}` — Get single snapshot

Returns the full details of a single snapshot by ID. Currently the frontend only lists snapshots by container — this gives direct access to a specific snapshot.

**Auth:** JWT required  
**Response:** `ApiResponse<SnapshotResponse>`

```typescript
// No frontend function exists yet
// You'd need to add: apiClient.get(`/api/v1/snapshots/${id}`)
```

**Use case:** Navigating directly to a snapshot detail page, or referencing a snapshot from a URL.

---

## Tags

### `GET /api/v1/tags/{id}` — Get single tag

Returns a single tag by ID with `{ id, name, color, containerCount }`.

**Auth:** JWT required  
**Response:** `ApiResponse<TagResponse>`

```typescript
// Frontend API already has the function:
tagsApi.get(id: string)
// Currently not called from any page
```

**Use case:** Navigating to a tag detail page, or viewing tag info from a URL.

---

### `PUT /api/v1/tags/{id}` — Update tag

Updates a tag's name and/or color.

**Auth:** JWT required  
**Request body:**
```json
{
  "name": "new-name",
  "color": "#ff0000"
}
```
Both fields are optional.

**Response:** `ApiResponse<TagResponse>`

```typescript
// Frontend API already has the function:
tagsApi.update(id: string, payload: { name?: string; color?: string })
// Currently not called from any page
```

**Use case:** Inline tag renaming or color picker in the tag list UI.

---

### `GET /api/v1/tags/search?q=` — Search tags

Searches tags by name (partial, case-insensitive match). Returns all matching tags for the current user.

**Auth:** JWT required  
**Query params:** `q` (string, required)  
**Response:** `ApiResponse<TagResponse[]>`

```typescript
// Frontend API already has the function:
tagsApi.search(q: string)
// Currently not called from any page
```

**Use case:** A tag search bar for quickly finding tags when a user has many.

---

### `GET /api/v1/tags/autocomplete?q=` — Tag autocomplete

Returns top 10 matching tags for autocomplete dropdowns. Same as search but limited to 10 results, designed for real-time suggestions as the user types.

**Auth:** JWT required  
**Query params:** `q` (string, required)  
**Response:** `ApiResponse<TagResponse[]>`

```typescript
// Frontend API already has the function:
tagsApi.autocomplete(q: string)
// Currently not called from any page
```

**Use case:** Tag input fields with autocomplete suggestions (e.g., when creating/editing a container).

---

## Timeline

### `GET /api/v1/timeline/{id}` — Get single timeline event

Returns a single timeline event by ID.

**Auth:** JWT required  
**Response:** `ApiResponse<TimelineEvent>`

```typescript
// Frontend API already has the function:
timelineApi.get(id: string)
// Currently not called from any page
```

**Use case:** Navigating to a specific timeline event detail view, or linking from notifications.

---

## Summary

| Priority | Endpoint | Frontend Function | What's Missing |
|----------|----------|-------------------|----------------|
| 🔵 High | `GET /api/v1/tags/autocomplete` | `tagsApi.autocomplete()` | Tag input UI on container create/edit forms |
| 🔵 High | `GET /api/v1/tags/search` | `tagsApi.search()` | Tag search/filter bar |
| 🟡 Medium | `PUT /api/v1/tags/{id}` | `tagsApi.update()` | Edit tag dialog (name + color picker) |
| 🟡 Medium | `POST /api/v1/containers/{id}/restore` | `containersApi.restore()` | Undo delete or trash view |
| 🟢 Low | `GET /api/v1/tags/{id}` | `tagsApi.get()` | Tag detail view / direct link |
| 🟢 Low | `GET /api/v1/snapshots/{id}` | Not yet added | Snapshot detail view |
| 🟢 Low | `GET /api/v1/timeline/{id}` | `timelineApi.get()` | Timeline event detail |
| 🔴 Admin | `DELETE /api/v1/containers/{id}/hard` | Not yet added | Admin purge UI (requires ADMIN role) |
