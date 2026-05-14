import type { ArticleVO, ArticleCreateDTO } from '~/types'

export function useArticle() {
  const api = useApi()

  function getArticle(id: number): Promise<ArticleVO> {
    return api.get<ArticleVO>(`/api/articles/${id}`, false)
  }

  function createArticle(dto: ArticleCreateDTO): Promise<number> {
    return api.post<number>('/api/articles/create', dto)
  }

  function improve(content: string): Promise<string> {
    return api.post<string>('/api/articles/improve', content)
  }

  return { getArticle, createArticle, improve }
}
