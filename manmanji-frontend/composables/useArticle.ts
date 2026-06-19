import type { ArticleVO, ArticleEditorVO, ArticleSaveDTO, ArticlePublishDTO, StudyArticle, ArticleManage, PageDTO, GroupVO, TagVO } from '~/types'

export function useArticle() {
  const api = useApi()

  /** 获取用户文章列表 */
  function listStudyArticles(): Promise<StudyArticle[]> {
    return api.get<StudyArticle[]>('/api/articles/titles')
  }

  /** 获取内容管理列表（含封面、摘要、统计，分页） */
  function listArticleManages(page: number, size: number, status?: string): Promise<PageDTO<ArticleManage>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) })
    if (status && status !== 'ALL') params.set('status', status)
    return api.get<PageDTO<ArticleManage>>(`/api/articles/manage?${params.toString()}`)
  }

  /** 获取所有标签 */
  function listTags(): Promise<TagVO[]> {
    return api.get<TagVO[]>('/api/articles/tags')
  }

  /** 获取用户已有分组 */
  function listGroups(): Promise<GroupVO[]> {
    return api.get<GroupVO[]>('/api/articles/groups')
  }

  /** 获取单篇文章 */
  function getArticle(id: number): Promise<ArticleVO> {
    return api.get<ArticleVO>(`/api/articles/${id}`)
  }

  /** 获取编辑器回显数据（含标签和分组） */
  function getArticleForEditor(id: number): Promise<ArticleEditorVO> {
    return api.get<ArticleEditorVO>(`/api/articles/editor/${id}`)
  }

  /** 创建文章 */
  function createArticle(): Promise<number> {
    return api.post<number>('/api/articles/create')
  }

  /** 保存文章（新建返回文章ID，更新返回原ID） */
  function saveArticle(dto: ArticleSaveDTO): Promise<number> {
    return api.put<number>('/api/articles/save', dto)
  }

  /** 发布文章（新建或更新并提交审核），返回文章 ID */
  function publishArticle(dto: ArticlePublishDTO): Promise<number> {
    return api.post<number>('/api/articles/publish', dto)
  }

  /** AI 润色文章内容（需要登录） */
  function improve(content: string): Promise<string> {
    return api.post<string>('/api/articles/improve', content)
  }

  /** 上传图片 */
  function uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    return api.uploadFormData<string>('/api/storage/image', formData)
  }

  /** 新建分组，返回新分组 ID */
  function createGroup(name: string): Promise<number> {
    return api.post<number>('/api/articles/groups', { name })
  }

  /** 删除分组 */
  function deleteGroup(id: number): Promise<void> {
    return api.delete(`/api/articles/groups/${id}`)
  }

  return { getArticle, getArticleForEditor, createArticle, saveArticle, publishArticle, improve, uploadImage, listStudyArticles, listArticleManages, listTags, listGroups, createGroup, deleteGroup }
}
