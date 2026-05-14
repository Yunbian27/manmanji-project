import type { FolderTreeVO } from '~/types'

export function useFolder() {
  const api = useApi()

  function getFolders(): Promise<FolderTreeVO[]> {
    return api.get<FolderTreeVO[]>('/api/user/folders')
  }

  return { getFolders }
}
