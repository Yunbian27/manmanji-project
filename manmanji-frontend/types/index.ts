// ============================================================
// types/index.ts — TypeScript 类型定义
// 把后端 Java 的 DTO/VO 类映射为前端 TS 接口，编译时类型检查
// 相当于 Java 的 model/dto/ 和 model/vo/ 包
// ============================================================

// ---------- API 响应包装 ----------
// 对应后端的 Result<T> 类：{ code: 200, message: "success", data: {...} }
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

// ---------- 认证模块 (auth) ----------
// 登录请求体
export interface LoginDTO {
  account: string
  password: string
}

// 注册请求体
export interface RegisterDTO {
  username: string
  email: string
  password: string
  code: string
  nickname?: string
}

// 刷新令牌请求体
export interface RefreshDTO {
  refreshToken: string
}

// 登录/注册/刷新成功后统一返回此结构
export interface LoginVO {
  accessToken: string          // JWT，15 分钟有效
  refreshToken: string         // JWT，7 天有效
  expiresIn: number            // 过期时间（秒）
  user: UserInfo               // 嵌套用户信息
}

// 用户公开信息
export interface UserInfo {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl: string | null     // TypeScript 联合类型：字符串或null
  role: 'USER' | 'ADMIN'       // 字面量联合类型：只能是这两个值
}

// ---------- 文章模块 (article) ----------
// 文章基本数据（对应 /api/articles/{id} 返回）
export interface ArticleVO {
  id: number
  title: string
  slug: string                 // URL 友好的标题（如 java-virtual-threads）
  content: string              // Markdown 格式
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

// 创建文章提交的数据
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

// 文章详情（含嵌套的作者、分类、标签信息）
export interface ArticleDetailVO {
  id: number
  title: string
  slug: string
  content: string
  summary: string | null
  coverUrl: string | null
  author: AuthorInfo           // 嵌套对象，非扁平字段
  category: CategoryInfo | null
  tags: TagInfo[]              // 数组类型
  status: string
  sourceType: string
  sourcePrompt: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount: number
  isOriginal: boolean | null
  sourceUrl: string | null
  liked: boolean               // 当前用户是否已点赞
  bookmarked: boolean          // 当前用户是否已收藏
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
  color: string | null         // 标签颜色（如 #FF6600）
}

// ---------- 文件夹树 (user) ----------
// 文件夹树节点，children 是自身类型的数组 → 递归结构
export interface FolderTreeVO {
  id: number
  name: string
  children: FolderTreeVO[]     // 递归：子文件夹
  articles: ArticleItem[]      // 该文件夹下的文章
}

export interface ArticleItem {
  id: number
  title: string
  status: 'DRAFT' | 'PUBLISHED'
}

// ---------- LLM Provider ----------
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
  hasApiKey: boolean           // API Key 是否已存储（不暴露具体值）
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

// ---------- 分页 ----------
// 泛型接口：<T> 表示 records 可以是任意类型的数组
export interface PageDTO<T> {
  total: number
  page: number
  size: number
  records: T[]                 // T 由调用方指定，如 PageDTO<ArticleVO>
}

// ---------- 目录导航 ----------
// 从文章 Markdown 中提取的 h2/h3/h4 标题列表
export interface TocItem {
  id: string                   // 锚点 id，如 "heading-0"
  text: string                 // 标题文字
  level: 2 | 3 | 4 | 5 | 6       // 标题级别
}

// ---------- 评论（mock 类型，后端控制器暂未实现） ----------
export interface CommentVO {
  id: number
  articleId: number
  author: AuthorInfo
  content: string
  parentId: number | null      // 父评论 id，null 表示顶层评论
  likeCount: number
  createdAt: string
}
