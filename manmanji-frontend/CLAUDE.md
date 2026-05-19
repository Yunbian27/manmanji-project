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

**Backend**: Java Spring Boot at `localhost:8080`. Dev proxy `/api/*` → `http://localhost:8080` (configured in `nuxt.config.ts`). No Nitro server routes — all API goes to the Java backend. Backend architecture in `../docs/CLAUDE.md`.

**Design system**: Uber-inspired black-and-white duet. `#000000` as the sole brand color (primary CTAs, active indicators), `#ffffff` canvas, no secondary accent. The pill (`999px` border-radius) is the signature interactive shape. Cards use `16px` radius. Font is Inter with only two roles: weight 700 for display headings, weights 400/500 for text. No letter-spacing, no all-caps headlines (sentence-case only). No dark mode — single white theme.

**Authoritative design specs** (in `../docs/`):
- `DESIGN.md` — color, typography, spacing, shapes, components
- `BLUEPRINT.md` — page layouts, responsive breakpoints, component placement
- `SIDEBAR.md` — left sidebar folder tree (ex-app-shell-row pattern)

**Typography rule**: `--ink` (#000000) for headings AND body paragraphs (main reading text). `--body` (#5e5e5e) only for secondary text (captions, metadata, sidebar navigation). Token file: `assets/css/tokens.css`.

**CSS loading order** (defined in `nuxt.config.ts`): tokens.css → base.css → transitions.css

### Routing & Layout

- `pages/index.vue` → `/` (article reader, 3-column layout)
- `pages/write.vue` → `/write` (markdown editor, auth required)
- `pages/login.vue` → `/login` (auth form, uses `layouts/blank.vue` — no TopNav/Footer)
- `layouts/default.vue` renders: `<TopNav />` + `<slot />` + `<Footer />`
- `layouts/blank.vue` renders: bare `<slot />` (for login page only)
- `app.vue`: `<NuxtLayout><NuxtPage /></NuxtLayout>`

### Layout Behavior

The page scrolls as a single document (no independent column scrolling). Sidebars use `position: sticky; top: var(--nav-height)` to stay in view. Footer appears naturally at the document bottom after all content. AppLayout (`components/layout/AppLayout.vue`) is a 3-column flex container with `#left-sidebar` / default / `#right-sidebar` named slots. Left sidebar width is fixed at 250px; main content max-width is `1000px` (`--content-max`); right sidebar max-width is `220px` (`--right-sidebar-width`). Both side columns have `flex: 1` to center the middle content on wide screens.

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
- `useAuth()` — login, register, refresh, logout
- `useArticle()` — get/create article, AI polish
- `useFolder()` — folder tree, create/rename/delete/move folders **and** rename/delete/move articles (article mutations live here because they return the refreshed folder tree)

**Context menu**: `ContextMenu.vue` renders via `<Teleport>` to `body`. `pages/index.vue` `provide('openContextMenu', handler)` — the handler builds menu items by right-click target type (`blank`/`folder`/`article`). `LeftSidebar.vue` and `TreeFolder.vue` `inject` it and call on `@contextmenu`. Selection dispatches to `handleCreateFolder`, `handleRenameFolder`, `handleDeleteFolder`, `handleRenameArticle`, `handleDeleteArticle`.

**Modals (PromptModal / ConfirmModal)**: Custom modals (no browser-native dialogs). Both use `<Teleport to="body">` + `<Transition name="modal">`. State is managed via `reactive()` objects + Promise resolve pattern in `pages/index.vue`:
```ts
const prompt = reactive({ visible, title, placeholder, confirmText, loading, error })
let promptResolve: ((value: string | null) => void) | null = null

function showPrompt(config): Promise<string | null> {
  return new Promise(resolve => { promptResolve = resolve })
}
// Handler calls: const name = await showPrompt(...)
// If name is null → user cancelled
// If name is set → set loading=true, call API, on success close modal, on error set prompt.error
```
Same pattern for `ConfirmModal` with `confirm` reactive state + `showConfirmDialog()`.

**Drag-and-drop**: `vuedraggable` (SortableJS with `:force-fallback="true"`) handles folder/article reordering. Mirror element is fully transparent (`opacity:0`); a custom `drag-cursor-icon` div tracks the mouse via `mousemove` listener in `onDragStart`, cleaned up in `onDragEnd`. Two sort groups: `"folders"` and `"articles"` — folders and articles cannot cross-sort.

**Editor state**: The markdown editor (`/write`) uses `provide/inject` (Symbol key `EDITOR_KEY`) rather than Pinia. `createEditorState()` in `composables/useArticleEditor.ts` creates the full state; `provideEditor()` / `injectEditor()` wire it. Single-instance feature.

**Auth guard**: Client-only named middleware `middleware/auth.client.ts` redirects unauthenticated users to `/login?redirect=...`. Auth state hydrated via `plugins/auth.client.ts` (loads JWT from localStorage before middleware runs). Pinia plugin renamed to `plugins/01.pinia.ts` to force load order (numeric prefix before alphabetical).

### Key Components

| Component | Purpose |
|-----------|---------|
| `AppButton` | 3 variants: `primary` (black pill), `secondary` (white pill + border), `subtle` (gray pill). `icon` and `text` variants removed |
| `AppTag` | Single gray pill style. Yellow variant removed (no second accent) |
| `Footer` | Black full-width footer. 3-column grid (产品/资源/关于) + copyright bar |
| `LeftSidebar` | Folder tree sidebar. Actions (create/rename/delete) via right-click context menu only. Matches SIDEBAR.md spec |
| `TreeFolder` | Recursive folder tree node. Folder headers: 16px/500, 8px radius. Article items: 14px/400/body color. Active state: 3px black left bar + canvas-soft bg |
| `AppLayout` | 3-column flex layout. Natural document scroll, sticky sidebars |

### Type System

All TypeScript interfaces are in `types/index.ts`, mapping Java backend DTOs/VOs:
- `ApiResult<T>` — `{ code, message, data }`
- `FolderTreeVO` — `{ folders: FolderTree[], rootArticles: ArticleItem[] }`，顶级响应包装体
- `FolderTree` — `{ id, name, children: FolderTree[], articles: ArticleItem[] }`，递归文件夹节点
- `ArticleItem` — `{ id, title, status: 'UNPUBLISHED' | 'PUBLISHED' }`
- `ArticleVO` — full article with all metadata fields
- `TocItem` — `{ id, text, level: 2|3|4|5|6 }`
- `PageDTO<T>` — `{ total, page, size, records }`

### Responsive Breakpoints (BLUEPRINT §7)

| Breakpoint | Width | Behavior |
|-----------|-------|----------|
| Mobile | < 600px | Hamburger menu, left sidebar drawer, content full-width |
| Tablet | 600–1119px | Right sidebar hidden, left sidebar narrowed |
| Desktop | ≥ 1120px | Full 3-column layout |
