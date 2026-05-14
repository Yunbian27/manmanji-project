// === API Response Wrapper ===
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

// === Auth ===
export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  email: string
  password: string
  nickname?: string
}

export interface RefreshDTO {
  refreshToken: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl: string | null
  role: 'USER' | 'ADMIN'
}

// === Article ===
export interface ArticleVO {
  id: number
  title: string
  slug: string
  content: string
  summary: string | null
  coverUrl: string | null
  authorId: number
  folderId: number | null
  categoryId: number | null
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  sourceType: 'MANUAL' | 'AI_GENERATED'
  sourcePrompt: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount: number
  isOriginal: boolean | null
  sourceUrl: string | null
  createdAt: string
  updatedAt: string
  publishedAt: string | null
}

export interface ArticleCreateDTO {
  title: string
  content: string
  summary?: string
  coverUrl?: string
  categoryId?: number
  tagIds?: number[]
  isOriginal?: boolean
  sourceUrl?: string
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface ArticleDetailVO {
  id: number
  title: string
  slug: string
  content: string
  summary: string | null
  coverUrl: string | null
  author: AuthorInfo
  category: CategoryInfo | null
  tags: TagInfo[]
  status: string
  sourceType: string
  sourcePrompt: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount: number
  isOriginal: boolean | null
  sourceUrl: string | null
  liked: boolean
  bookmarked: boolean
  publishedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AuthorInfo {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
}

export interface CategoryInfo {
  id: number
  name: string
  slug: string
}

export interface TagInfo {
  id: number
  name: string
  slug: string
  color: string | null
}

// === Folder Tree ===
export interface FolderTreeVO {
  id: number
  name: string
  children: FolderTreeVO[]
  articles: ArticleItem[]
}

export interface ArticleItem {
  id: number
  title: string
  status: 'DRAFT' | 'PUBLISHED'
}

// === LLM Provider ===
export interface LlmProviderVO {
  id: string
  baseUrl: string
  model: string
  embeddingModel: string | null
  embeddingDimensions: number | null
  supportsEmbedding: boolean
  temperature: number | null
  enabled: boolean
  builtin: boolean
  hasApiKey: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateProviderDTO {
  id: string
  baseUrl: string
  model: string
  apiKey?: string
  embeddingModel?: string
  embeddingDimensions?: number
  supportsEmbedding?: boolean
  temperature?: number
}

// === Pagination ===
export interface PageDTO<T> {
  total: number
  page: number
  size: number
  records: T[]
}

// === TOC ===
export interface TocItem {
  id: string
  text: string
  level: 2 | 3 | 4
}

// === Comment (mock until backend implements controller) ===
export interface CommentVO {
  id: number
  articleId: number
  author: AuthorInfo
  content: string
  parentId: number | null
  likeCount: number
  createdAt: string
}
