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

// 登录/注册成功后统一返回此结构
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
  status: 'UNPUBLISHED' | 'PUBLISHED'
  visibility?: 'PUBLIC' | 'PRIVATE'
  sourceType: 'MANUAL' | 'AI_GENERATED'
  sourcePrompt: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount: number
  isOriginal: boolean | null
  sourceUrl: string | null
  articleType?: 'ORIGINAL' | 'REPOST'
  groupNames?: string[]
  creationStatement?: string
  createdAt: string
  updatedAt: string
  publishedAt: string | null
}

// 保存文章提交的数据
export interface ArticleSaveDTO {
  id: number
  title: string
  content: string
}

// 发布文章提交的数据
export interface ArticlePublishDTO {
  id: number
  title: string
  content: string
  summary?: string
  coverUrl?: string
  tagIds?: number[]
  groupNames?: string[]
  articleType?: 'ORIGINAL' | 'REPOST'
  sourceUrl?: string
  visibility?: 'PUBLIC' | 'PRIVATE'
  creationStatement?: string
}

// ---------- 文件夹树 (user) ----------

export interface ArticleItem {
  id: number
  title: string
  status: 'UNPUBLISHED' | 'PUBLISHED'
}

// 文件夹树节点，children 是自身类型的数组 → 递归结构
export interface FolderTree {
  id: number
  name: string
  children: FolderTree[]       // 递归：子文件夹
  articles: ArticleItem[]      // 该文件夹下的文章
}

// 文件夹树 API 响应
export interface FolderTreeVO {
  folders: FolderTree[]
  rootArticles: ArticleItem[]  // folder_id 为空的文章
}

// ---------- 目录导航 ----------
export interface TocItem {
  id: string
  text: string
  level: 2 | 3 | 4 | 5 | 6
}

// ---------- 书房（study） ----------
// 书房文章列表项，用于 home.vue
export interface StudyArticle {
  id: number
  title: string
  status: 'UNPUBLISHED' | 'PUBLISHED' | 'BOOKMARKED'
  visibility?: 'PUBLIC' | 'PRIVATE'
  tags: string[]                  // 标签名称列表
  updatedAt: string               // 最后修改时间
  // 仅收藏文章
  sourceAuthor?: string           // 原作者昵称
  bookmarkedAt?: string           // 收藏时间
}

