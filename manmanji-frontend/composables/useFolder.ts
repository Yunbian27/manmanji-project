// ============================================================
// composables/useFolder.ts — 文件夹 API 封装
// 对应后端 UserController 的 GET /api/user/folders
// ============================================================

import type { FolderTreeVO } from '~/types'

export function useFolder() {
  const api = useApi()

  /** 获取当前用户的文件夹树（需要登录） */
  function getFolders(): Promise<FolderTreeVO[]> {
    return api.get<FolderTreeVO[]>('/api/user/folders')
  }

  return { getFolders }
}
