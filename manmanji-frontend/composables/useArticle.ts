import type { ArticleVO, ArticleSaveDTO, ArticlePublishDTO, StudyArticle, GroupVO } from '~/types'

export function useArticle() {
  const api = useApi()

  /** 获取用户文章列表 */
  function listStudyArticles(): Promise<StudyArticle[]> {
    return api.get<StudyArticle[]>('/api/articles/titles')
  }

  /** 获取用户已有分组 */
  function listGroups(): Promise<GroupVO[]> {
    return api.get<GroupVO[]>('/api/articles/groups')
  }

  /** 获取单篇文章 */
  function getArticle(id: number): Promise<ArticleVO> {
    return api.get<ArticleVO>(`/api/articles/${id}`)
  }

  /** 创建文章 */
  function createArticle(): Promise<number> {
    return api.post<number>('/api/articles/create')
  }

  /** 保存文章（新建返回文章ID，更新返回原ID） */
  function saveArticle(dto: ArticleSaveDTO): Promise<number> {
    return api.put<number>('/api/articles/save', dto)
  }

  /** 发布文章（新建返回文章ID，更新返回原ID） */
  function publishArticle(dto: ArticlePublishDTO): Promise<number> {
    return api.put<number>('/api/articles/publish', dto)
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

  /** 新建分组 */
  function createGroup(name: string): Promise<void> {
    return api.post('/api/articles/groups', { name })
  }

  /** 删除分组 */
  function deleteGroup(name: string): Promise<void> {
    return api.delete(`/api/articles/groups/${encodeURIComponent(name)}`)
  }

  return { getArticle, createArticle, saveArticle, publishArticle, improve, uploadImage, listStudyArticles, listGroups, createGroup, deleteGroup }
}
