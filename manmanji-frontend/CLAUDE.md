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

**Backend**: Java Spring Boot at `localhost:8080`. Dev proxy `/api/*` → `http://localhost:8080` (configured in `nuxt.config.ts`). No Nitro server routes. Backend architecture in `../docs/CLAUDE.md`.

### Design System

Design tokens in `assets/css/tokens.css`, based on Notion DESIGN.md (`../docs/DESIGN.md`):
- **Primary**: `#2563eb` (blue), pressed `#1d4ed8`
- **Canvas**: `#ffffff`, **Surface**: `#f6f5f4`, **Surface Soft**: `#fafaf9` (page backgrounds)
- **Text**: `--ink` (#1a1a1a) for body, `--slate`/`--steel`/`--muted` for secondary
- **Buttons**: 8px rounded (`--rounded-md`), **not** pill-shaped
- **Cards**: 12px rounded (`--rounded-lg`), shadow level 2: `rgba(15,15,15,0.08) 0px 4px 12px`
- **Font**: Inter (sans), JetBrains Mono (mono)
- **Spacing**: 4px base, scale: 8/12/16/20/24/32/40
- **Transition**: `cubic-bezier(0.16, 1, 0.3, 1)`

CSS loading order (`nuxt.config.ts`): `tokens.css` → `base.css` → `transitions.css`

### Routing & Layouts

| Route | Page | Layout | Notes |
|-------|------|--------|-------|
| `/` | `index.vue` | `default` | Landing page; redirects to `/home` if authenticated |
| `/home` | `home.vue` | `blank` | 2-column 语雀-style (sidebar + content), auth required |
| `/write` | `write.vue` | `editor` | Markdown editor, auth required, `?articleId=X` to edit |
| `/login` | `login.vue` | `blank` | Auth form |

- `layouts/default.vue`: `<TopNav />` + `<slot />` + `<Footer />`
- `layouts/blank.vue`: bare `<slot />` (used by home and login)
- `layouts/editor.vue`: `100vh` container with `background: var(--surface-soft)`, `overflow: hidden`

### Auto-imports

Nuxt auto-imports all Vue composables (`ref`, `computed`, `watch`, `provide`, `inject`, etc.):
- Components from `components/` subdirectories are globally available without prefix (e.g., `<AppButton />`, `<EditorView />`, `<IconX />`)
- Stores from `stores/` are auto-imported (e.g., `useAuthStore()`)
- Composables from `composables/` are auto-imported (e.g., `useArticle()`, `useApi()`)
- `defineStore` is NOT auto-imported — each store file must `import { defineStore } from 'pinia'`

### Home Page (`home.vue`)

Two-column 语雀-style layout using `layouts/blank.vue`:
- **Left sidebar (280px)**: Logo, tag filter popover, 4 status filter buttons (全部/已发布/草稿/收藏), scrollable article list, "探索社区" footer button
- **Right content area**: 51px topbar (search input, article title, "发布文章" button, avatar dropdown), scrollable article content card (`max-width: 720px`)

All state is local to the page (no provide/inject). "发布文章" opens `/write` in a new tab via `window.open('/write', '_blank')`. Article list uses mock data from `useArticle().listStudyArticles()`.

### Editor Architecture (`/write`)

**Component hierarchy:**
```
EditorView (provides EDITOR_KEY)
├── EditorTopNav — back button, title input, view mode toggle (edit/split/preview), avatar dropdown
├── EditorToolbar — format buttons | insert buttons | more actions. Emits format(before, after, placeholder)
├── EditorTextarea (card) — textarea with v-model="content", exposes syncScroll/insertAtCursor
├── DraggableDivider — resizes split panes (16px invisible zone, hover handle)
├── EditorPreview (card) — rendered HTML, exposes syncScroll
├── RightFloatingPanel — 3 circular buttons → slide-out panels (TOC/AI/Help)
├── BottomStatusBar — word/line/cursor stats + save draft/publish buttons
└── PublishSettingsModal — category, tags, cover, summary, isOriginal
```

**State sharing**: Uses `provide/inject` with `Symbol('editor')` key (`EDITOR_KEY`), not Pinia. `EditorView` calls `createEditorState(articleId)` and `provideEditor()`. All child components call `injectEditor()` to access shared state (`title`, `content`, `viewMode`, `isSaving`, `lastSavedAt`, `publishError`, `publishSettings`, `save()`, `publish()`, etc.).

**Card layout**: Page background is `--surface-soft`, editor/preview are white `--canvas` cards with `--rounded-lg` + level-2 shadow, separated by `gap: 16px` in split mode, centered `max-width: 820px` in single mode.

**Scroll sync**: Bidirectional between textarea and preview using guard flags (`isTextareaDriven`/`isPreviewDriven`) and `requestAnimationFrame` to prevent feedback loops.

**Markdown rendering**: `useMarkdownRenderer(content)` → MarkdownIt + highlight.js → `renderedHtml` + `tocItems`. Code blocks get a header with language label and "copy code" button. Headings get auto-generated IDs for TOC navigation.

**Image upload**: EditorToolbar image button → `<input type="file">` → `useArticle().uploadImage(file)` → `POST /api/storage/image` multipart → returns URL → `insertAtCursor('![', '](url)', '图片描述')`.

### API Layer

`composables/useApi.ts` wraps `fetch` with JWT injection and `ApiResult<T>` unwrapping:
- `request<T>()` / `get/post/put/delete` — JSON requests
- `uploadFormData<T>()` — multipart file uploads

Domain composables delegate to `useApi()`:
- `useAuth()` — login, register, refresh, logout
- `useArticle()` — get/create/save/publish article, AI polish, `uploadImage(file)`
- `useFolder()` — folder tree CRUD + article rename/delete/move

### Auth

- Global middleware `auth.global.ts` runs on every route, redirects unauthenticated users to `/login?redirect=...`
- `plugins/01.pinia.ts` (numeric prefix forces load order before alphabetical plugins)
- `plugins/auth.client.ts` hydrates JWT from localStorage before middleware runs
- Access + refresh token pattern (15min / 7 days)

### Drag-and-Drop

`vuedraggable` (SortableJS) with `:force-fallback="true"` handles folder/article reordering in home page sidebar. Custom drag cursor with fully transparent mirror element.

### Type System

All interfaces in `types/index.ts`, mapping Java backend DTOs:
- `ApiResult<T>` — `{ code, message, data }`
- `ArticleVO`, `ArticleSaveDTO`, `ArticlePublishDTO`
- `FolderTreeVO`, `FolderTree`, `ArticleItem`
- `TocItem` — `{ id, text, level: 2|3|4|5|6 }`
- `PageDTO<T>` — `{ total, page, size, records }`

### Components

| Component | Role |
|-----------|------|
| `AppButton` | `primary` (blue), `secondary` (white + border), `subtle` (ghost) |
| `AppTag` | Chip/tag with surface background |
| `TopNav` | Global nav bar (used in `default` layout only) |
| `Footer` | Full-width footer (used in `default` layout only) |
| `EditorView` | Editor orchestrator — provides EDITOR_KEY, manages split ratio and scroll sync |
| `EditorTopNav` | Persistent top bar — back, title, view toggle, avatar |
| `EditorToolbar` | Fixed format toolbar — format/insert/more actions |
| `EditorTextarea` | Markdown textarea — exposes `insertAtCursor`, `syncScroll`, `getCursorLineCol` |
| `EditorPreview` | Rendered HTML preview — exposes `syncScroll` |
| `DraggableDivider` | Split pane resize handle — transparent with hover indicator |
| `BottomStatusBar` | Semi-transparent bottom bar — stats + save/publish |
| `RightFloatingPanel` | Right edge circular buttons + 320px slide-out panels (TOC/AI/Help) |
| `PublishSettingsModal` | Modal — category, tags, cover, summary, isOriginal |
| `IconChevronRight` | Lucide chevron-right SVG (default size 16) |
| `IconSearch` | Lucide search SVG (default size 16) |
| `IconX` | Lucide x SVG (default size 20) |

### Responsive Breakpoints

| Breakpoint | Width | Behavior |
|-----------|-------|----------|
| Mobile | < 600px | Hide logo text, shrink inputs, hide toolbar right section |
| Tablet | 600–768px | Hide toolbar button labels |
| Desktop | ≥ 768px | Full layout |
