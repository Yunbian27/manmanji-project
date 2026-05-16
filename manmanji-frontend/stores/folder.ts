import { defineStore } from 'pinia'
import type { FolderTreeVO } from '~/types'

export const useFolderStore = defineStore('folder', () => {
  const folders = ref<FolderTreeVO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const collator = new Intl.Collator('zh-CN')

  function sortTree(tree: FolderTreeVO[]) {
    tree.sort((a, b) => collator.compare(a.name, b.name))
    for (const f of tree) {
      f.children.sort((a, b) => collator.compare(a.name, b.name))
      f.articles.sort((a, b) => collator.compare(a.title, b.title))
      sortTree(f.children)
    }
  }

  async function fetchFolders() {
    loading.value = true
    error.value = null
    try {
      const { getFolders } = useFolder()
      folders.value = await getFolders()
      sortTree(folders.value)
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

  async function moveFolder(id: number, newParentId: number | null) {
    const { moveFolder: apiMove } = useFolder()
    folders.value = await apiMove(id, newParentId)
    sortTree(folders.value)
  }

  async function moveArticle(id: number, newFolderId: number | null) {
    const { moveArticle: apiMove } = useFolder()
    folders.value = await apiMove(id, newFolderId)
    sortTree(folders.value)
  }

  return { folders, loading, error, fetchFolders, createFolder, renameFolder, deleteFolder, renameArticle, deleteArticle, moveFolder, moveArticle }
})
