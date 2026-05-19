import { defineStore } from 'pinia'
import type { FolderTree, FolderTreeVO, ArticleItem } from '~/types'

export const useFolderStore = defineStore('folder', () => {
  const folders = ref<FolderTree[]>([])
  const rootArticles = ref<ArticleItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const expandedIds = ref<number[]>([])
  const collator = new Intl.Collator('zh-CN')

  function sortTree(tree: FolderTree[]) {
    tree.sort((a, b) => collator.compare(a.name, b.name))
    for (const f of tree) {
      f.children.sort((a, b) => collator.compare(a.name, b.name))
      f.articles.sort((a, b) => collator.compare(a.title, b.title))
      sortTree(f.children)
    }
  }

  function applyResult(result: FolderTreeVO) {
    folders.value = result.folders
    rootArticles.value = result.rootArticles.sort((a, b) => collator.compare(a.title, b.title))
    sortTree(folders.value)
  }

  async function fetchFolders() {
    loading.value = true
    error.value = null
    try {
      const { getFolders } = useFolder()
      applyResult(await getFolders())
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载文件夹失败'
    } finally {
      loading.value = false
    }
  }

  async function createFolder(name: string, parentId?: number) {
    const { createFolder: apiCreate } = useFolder()
    applyResult(await apiCreate(name, parentId))
  }

  async function renameFolder(id: number, name: string) {
    const { renameFolder: apiRename } = useFolder()
    applyResult(await apiRename(id, name))
  }

  async function deleteFolder(id: number) {
    const { deleteFolder: apiDelete } = useFolder()
    applyResult(await apiDelete(id))
  }

  async function renameArticle(id: number, title: string) {
    const { renameArticle: apiRename } = useFolder()
    applyResult(await apiRename(id, title))
  }

  async function deleteArticle(id: number) {
    const { deleteArticle: apiDelete } = useFolder()
    applyResult(await apiDelete(id))
  }

  async function moveFolder(id: number, newParentId: number | null) {
    const { moveFolder: apiMove } = useFolder()
    applyResult(await apiMove(id, newParentId))
  }

  async function moveArticle(id: number, newFolderId: number | null) {
    const { moveArticle: apiMove } = useFolder()
    applyResult(await apiMove(id, newFolderId))
  }

  return { folders, rootArticles, loading, error, expandedIds, fetchFolders, createFolder, renameFolder, deleteFolder, renameArticle, deleteArticle, moveFolder, moveArticle }
})
