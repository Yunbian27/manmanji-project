import { defineStore } from 'pinia'
import type { FolderTreeVO } from '~/types'

export const useFolderStore = defineStore('folder', () => {
  const folders = ref<FolderTreeVO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchFolders() {
    loading.value = true
    error.value = null
    try {
      const { getFolders } = useFolder()
      folders.value = await getFolders()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载文件夹失败'
    } finally {
      loading.value = false
    }
  }

  return { folders, loading, error, fetchFolders }
})
