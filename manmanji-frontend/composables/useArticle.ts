import type { ArticleVO, ArticleSaveDTO, ArticlePublishDTO } from '~/types'

export function useArticle() {
  const api = useApi()

  /** 获取单篇文章 */
  function getArticle(id: number): Promise<ArticleVO> {
    return api.get<ArticleVO>(`/api/articles/${id}`)
  }

  /** 创建文章（folderId 可选，不传则无文件夹） */
  function createArticle(folderId?: number): Promise<number> {
    if (folderId != null) {
      return api.post<number>(`/api/articles/create/${folderId}`)
    }
    return api.post<number>('/api/articles/create')
  }

  /** 保存文章 */
  function saveArticle(dto: ArticleSaveDTO): Promise<void> {
    return api.put<void>('/api/articles/save', dto)
  }

  /** 发布文章 */
  function publishArticle(dto: ArticlePublishDTO): Promise<void> {
    return api.put<void>('/api/articles/publish', dto)
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

  return { getArticle, createArticle, saveArticle, publishArticle, improve, uploadImage }
}
