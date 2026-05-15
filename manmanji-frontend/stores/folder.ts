import { defineStore } from 'pinia'
import type { FolderTreeVO } from '~/types'

export const useFolderStore = defineStore('folder', () => {
  const folders = ref<FolderTreeVO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const folderMap = shallowRef<Map<number, FolderTreeVO>>(new Map())

  function buildFolderMap(tree: FolderTreeVO[]) {
    const map = new Map<number, FolderTreeVO>()
    function walk(list: FolderTreeVO[]) {
      for (const f of list) {
        map.set(f.id, f)
        walk(f.children)
      }
    }
    walk(tree)
    folderMap.value = map
  }

  async function fetchFolders() {
    loading.value = true
    error.value = null
    try {
      const { getFolders } = useFolder()
      folders.value = await getFolders()
      buildFolderMap(folders.value)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载文件夹失败'
    } finally {
      loading.value = false
    }
  }

  async function createFolder(name: string, parentId?: number) {
    const { createFolder: apiCreate } = useFolder()
    await apiCreate(name, parentId)
    await fetchFolders()
  }

  async function renameFolder(id: number, name: string) {
    const { renameFolder: apiRename } = useFolder()
    await apiRename(id, name)
    await fetchFolders()
  }

  async function deleteFolder(id: number) {
    const { deleteFolder: apiDelete } = useFolder()
    await apiDelete(id)
    await fetchFolders()
  }

  async function renameArticle(id: number, title: string) {
    const { renameArticle: apiRename } = useFolder()
    await apiRename(id, title)
    await fetchFolders()
  }

  async function deleteArticle(id: number) {
    const { deleteArticle: apiDelete } = useFolder()
    await apiDelete(id)
    await fetchFolders()
  }

  async function moveFolder(id: number, newParentId: number | null, sortOrder: number) {
    const { moveFolder: apiMove } = useFolder()
    await apiMove(id, newParentId, sortOrder)
    await fetchFolders()
  }

  async function moveArticle(id: number, newFolderId: number, sortOrder: number) {
    const { moveArticle: apiMove } = useFolder()
    await apiMove(id, newFolderId, sortOrder)
    await fetchFolders()
  }

  return { folders, loading, error, folderMap, fetchFolders, createFolder, renameFolder, deleteFolder, renameArticle, deleteArticle, moveFolder, moveArticle }
})
