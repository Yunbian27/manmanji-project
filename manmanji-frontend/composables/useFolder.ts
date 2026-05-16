import type { FolderTreeVO } from '~/types'

export function useFolder() {
  const api = useApi()

  function getFolders(): Promise<FolderTreeVO[]> {
    return api.get<FolderTreeVO[]>('/api/folders')
  }

  function createFolder(name: string, parentId?: number): Promise<FolderTreeVO> {
    return api.post<FolderTreeVO>('/api/folders', { name, parentId })
  }

  function renameFolder(id: number, name: string): Promise<void> {
    return api.put<void>(`/api/folders/${id}`, { name })
  }

  function deleteFolder(id: number): Promise<void> {
    return api.delete<void>(`/api/folders/${id}`)
  }

  function renameArticle(id: number, title: string): Promise<void> {
    return api.put<void>(`/api/articles/${id}`, { title })
  }

  function deleteArticle(id: number): Promise<void> {
    return api.delete<void>(`/api/articles/${id}`)
  }

  function moveFolder(id: number, parentId: number | null): Promise<FolderTreeVO[]> {
    return api.put<FolderTreeVO[]>('/api/folders/move', { folderId: id, parentId })
  }

  function moveArticle(id: number, folderId: number | null): Promise<FolderTreeVO[]> {
    return api.put<FolderTreeVO[]>('/api/articles/move', { articleId: id, folderId })
  }

  return { getFolders, createFolder, renameFolder, deleteFolder, renameArticle, deleteArticle, moveFolder, moveArticle }
}
