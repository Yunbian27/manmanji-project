# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev          # Development server (http://localhost:3000)
npm run build        # Production build
npm run preview      # Preview production build
npx nuxi typecheck   # TypeScript type check
```

## Architecture

**Stack**: Nuxt 4.1 + Vue 3.5 (Composition API, `<script setup>`) + TypeScript + Pinia 3

**Backend**: Java Spring Boot at `localhost:8080`. During dev, Nitro proxies `/api/*` → `http://localhost:8080` (configured in `nuxt.config.ts`). No Nitro server routes exist — all API goes to the Java backend. Backend architecture details in `../docs/CLAUDE.md`.

**Design system**: ClickHouse-inspired dark-first design with electric yellow (`#faff69`) as the sole brand accent. The authoritative design spec is `../docs/DESIGN.md` — all UI changes must reference it. Key constraints: no drop shadows, `round-md`(8px) for buttons, `round-lg`(12px) for cards, yellow scarce (primary CTAs + focus borders only), Inter 700/600/400 as the only font family. All tokens are in `assets/css/tokens.css`. Light theme overrides in `assets/css/light-theme.css`. Theme stored as `mannote-theme` in localStorage; inline script in `nuxt.config.ts` sets `data-theme` attr before Vue mounts to prevent FOUC.

**CSS loading order** (defined in `nuxt.config.ts`): tokens.css → base.css → transitions.css → light-theme.css

### Routing & Layout

- `pages/index.vue` → `/` (article reader)
- `pages/write.vue` → `/write` (markdown editor)
- `layouts/default.vue` wraps every page: `<TopNav />` + `<slot />`
- `app.vue` renders: `<NuxtLayout><NuxtPage /></NuxtLayout>`

### Auto-imports

Nuxt auto-imports all Vue composables (`ref`, `computed`, `watch`, `provide`, `inject`, etc.), plus:
- Components from all subdirectories under `components/` are globally available **without prefix** (e.g., `<TopNav />`, `<AppButton />`, `<TreeFolder />`)
- Stores from `stores/` are auto-imported (e.g., `useAuthStore()` can be called without importing)

The `defineStore` function is NOT auto-imported — each store file must `import { defineStore } from 'pinia'`.

### Key Patterns

**Three-state async data** (used everywhere: stores, components):
```
loading → skeleton | error → message + retry | data → render
```

**API layer**: `composables/useApi.ts` wraps `fetch` with JWT injection and `ApiResult<T>` unwrapping. Domain composables delegate to `useApi()` and return typed promises:
- `useAuth()` — login, register, refresh
- `useArticle()` — get/create article, AI polish
- `useFolder()` — folder tree, create/rename/delete/move folders **and** rename/delete/move articles (article mutations live here because they return the refreshed folder tree)

**Context menu**: `ContextMenu.vue` renders via `<Teleport>` to `body`. `pages/index.vue` `provide('openContextMenu', handler)` — the handler builds menu items by right-click target type (`blank`/`folder`/`article`). `LeftSidebar.vue` and `TreeFolder.vue` `inject` it and call on `@contextmenu`. Selection dispatches to `handleCreateFolder`, `handleRenameFolder`, `handleDeleteFolder`, `handleRenameArticle`, `handleDeleteArticle`.

**Modals (PromptModal / ConfirmModal)**: Custom modals replace browser-native `window.prompt()`/`window.confirm()`/`window.alert()`. Both use `<Teleport to="body">` + `<Transition name="modal">` (matching `LoginModal.vue` pattern). State is managed via `reactive()` objects + Promise resolve pattern in `pages/index.vue`:
```ts
const prompt = reactive({ visible, title, placeholder, confirmText, loading, error })
let promptResolve: ((value: string | null) => void) | null = null

function showPrompt(config): Promise<string | null> {
  // set config fields, set visible=true
  return new Promise(resolve => { promptResolve = resolve })
}
// Handler calls: const name = await showPrompt(...)
// If name is null → user cancelled
// If name is set → set loading=true, call API, on success close modal, on error set prompt.error
```
Same pattern for `ConfirmModal` with `confirm` reactive state + `showConfirmDialog()`.

**Drag-and-drop**: `vuedraggable` (SortableJS with `:force-fallback="true"`) handles folder/article reordering. Mirror element is fully transparent (`opacity:0`); a custom `drag-cursor-icon` div tracks the mouse via `mousemove` listener in `onDragStart`, cleaned up in `onDragEnd`. Two sort groups: `"folders"` and `"articles"` — folders and articles cannot cross-sort.

**Editor state**: The markdown editor (`/write`) uses `provide/inject` (Symbol key `EDITOR_KEY`) rather than Pinia. `createEditorState()` in `composables/useArticleEditor.ts` creates the full state; `provideEditor()` / `injectEditor()` wire it. Single-instance feature.

### Component Inventory

| Component | Purpose |
|-----------|---------|
| `PromptModal` | Text input dialog (create/rename folder, rename article). Follows `text-input` + `button-primary` specs |
| `ConfirmModal` | Delete confirmation dialog. Red danger button (`error` bg), no input |
| `LoginModal` | Auth: username + password + submit |
| `ContextMenu` | Right-click menu, positioned absolutely, auto-adjusts to viewport |
| `LeftSidebar` | Folder tree sidebar with loading/error/empty/data states + root-level draggable |
| `TreeFolder` | Recursive folder tree node with child folders draggable + articles draggable |
| `AppLayout` | Three-column flex layout with `#left-sidebar` / default / `#right-sidebar` slots |

### Type System

All TypeScript interfaces are in `types/index.ts`, mapping Java backend DTOs/VOs:
- `ApiResult<T>` — `{ code, message, data }`
- `FolderTreeVO` — recursive: `{ id, name, children: FolderTreeVO[], articles: ArticleItem[] }`
- `ArticleItem` — `{ id, title, status: 'DRAFT' | 'PUBLISHED' }`
- `ArticleVO` — full article with all metadata fields
- `ArticleCreateDTO` — `{ title, content, summary?, coverUrl?, folderId?, categoryId?, tagIds?, isOriginal?, sourceUrl?, status? }`
- `TocItem` — `{ id, text, level: 2|3|4|5|6 }`
- `PageDTO<T>` — `{ total, page, size, records }`

### Responsive Breakpoints

`useDevice()` composable provides:
- `isMobile`: < 768px — hamburger menu, full-screen sidebar drawer
- `isTablet`: < 1024px — right sidebar hidden, left sidebar narrowed
- `isDesktop`: ≥ 1024px

Three-column layout (`AppLayout.vue`) uses flexbox with named slots.
