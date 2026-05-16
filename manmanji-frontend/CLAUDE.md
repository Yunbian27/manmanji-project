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

**Backend**: Java Spring Boot at `localhost:8080`. During dev, Nitro proxies `/api/*` → `http://localhost:8080` (configured in `nuxt.config.ts`). No Nitro server routes exist — all API goes to the Java backend.

**Theme**: Dark-first design with electric yellow (`#faff69`) as the sole brand accent. All colors/spacing/radii/shadow tokens are in `assets/css/tokens.css`. Light theme overrides in `assets/css/light-theme.css`. Theme stored as `mannote-theme` in localStorage; inline script in `nuxt.config.ts` sets `data-theme` attr before Vue mounts to prevent FOUC.

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

The `defineStore` function however is NOT auto-imported — each store file must `import { defineStore } from 'pinia'`.

### Key Patterns

**Three-state async data** (used everywhere: stores, components):
```
loading → skeleton | error → message + retry | data → render
```

**API layer**: `composables/useApi.ts` wraps `fetch` with JWT injection and `ApiResult<T>` unwrapping. Domain composables (`useAuth`, `useArticle`, `useFolder`) delegate to `useApi()` and return typed promises.

**Editor state**: The markdown editor (`/write`) uses `provide/inject` (Symbol key `EDITOR_KEY`) rather than Pinia. `createEditorState()` in `composables/useArticleEditor.ts` creates the full state object; `provideEditor()` / `injectEditor()` wire it through the component tree. The editor is a single-instance feature.

**Drag-and-drop**: `vuedraggable` (SortableJS) handles folder tree reordering in `TreeFolder.vue`. Since Vue 3 props are readonly, TreeFolder maintains local writable refs synced to the Pinia `folderStore` via a `folderMap` (shallowRef Map for O(1) folder lookup). Changes trigger `moveFolder`/`moveArticle` store actions → backend API → `fetchFolders()` refresh.

**Context menu**: `components/common/ContextMenu.vue` renders via `<Teleport>` to `body`. Handler function is `provide('openContextMenu')` from `pages/index.vue`, injected by `LeftSidebar.vue` and `TreeFolder.vue`. Menu items are built dynamically based on right-click target type (blank/folder/article).

### Type System

All TypeScript interfaces are in `types/index.ts`, mapping Java backend DTOs/VOs:
- `ApiResult<T>` — unified response wrapper `{ code, message, data }`
- `FolderTreeVO` — recursive: `{ id, name, children: FolderTreeVO[], articles: ArticleItem[] }`
- `ArticleItem` — `{ id, title, status: 'DRAFT' | 'PUBLISHED' }`
- `ArticleVO` — full article with all metadata fields
- `TocItem` — `{ id, text, level: 2|3|4|5|6 }`
- `PageDTO<T>` — `{ total, page, size, records }`

### Responsive Breakpoints

`useDevice()` composable provides:
- `isMobile`: < 768px — hamburger menu, full-screen sidebar drawer
- `isTablet`: < 1024px — right sidebar hidden, left sidebar narrowed
- `isDesktop`: ≥ 1024px

Three-column layout (`AppLayout.vue`) uses flexbox with named slots: `#left-sidebar`, default (main), `#right-sidebar`.
