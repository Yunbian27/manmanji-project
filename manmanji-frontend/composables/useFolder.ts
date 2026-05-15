import type { FolderTreeVO } from '~/types'

export function useFolder() {
  const api = useApi()

  function getFolders(): Promise<FolderTreeVO[]> {
    return api.get<FolderTreeVO[]>('/api/user/folders')
  }

  function createFolder(name: string, parentId?: number): Promise<FolderTreeVO> {
    return api.post<FolderTreeVO>('/api/user/folders', { name, parentId })
  }

  function renameFolder(id: number, name: string): Promise<void> {
    return api.put<void>(`/api/user/folders/${id}`, { name })
  }

  function deleteFolder(id: number): Promise<void> {
    return api.delete<void>(`/api/user/folders/${id}`)
  }

  function renameArticle(id: number, title: string): Promise<void> {
    return api.put<void>(`/api/articles/${id}`, { title })
  }

  function deleteArticle(id: number): Promise<void> {
    return api.delete<void>(`/api/articles/${id}`)
  }

  function moveFolder(id: number, newParentId: number | null, sortOrder: number): Promise<void> {
    return api.put<void>(`/api/user/folders/${id}/move`, { newParentId, sortOrder })
  }

  function moveArticle(id: number, newFolderId: number, sortOrder: number): Promise<void> {
    return api.put<void>(`/api/articles/${id}/move`, { newFolderId, sortOrder })
  }

  return { getFolders, createFolder, renameFolder, deleteFolder, renameArticle, deleteArticle, moveFolder, moveArticle }
}
