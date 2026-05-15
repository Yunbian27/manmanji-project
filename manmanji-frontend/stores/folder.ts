// ============================================================
// stores/folder.ts — 文件夹树状态管理（Pinia Store）
// 管理左侧栏的文件夹树数据，含 loading/error 状态
//
// 三种状态模式（项目所有异步数据都用这个模式）：
// 1. loading = true  → 显示骨架屏/加载动画
// 2. error != null   → 显示错误提示 + 重试按钮
// 3. folders 有数据  → 正常渲染文件夹树
// ============================================================

import { defineStore } from 'pinia'
import type { FolderTreeVO } from '~/types'

export const useFolderStore = defineStore('folder', () => {
  const folders = ref<FolderTreeVO[]>([])          // 文件夹树数据
  const loading = ref(false)                       // 是否正在加载
  const error = ref<string | null>(null)           // 错误信息

  /** 从后端获取文件夹树 */
  async function fetchFolders() {
    loading.value = true
    error.value = null
    try {
      // useFolder() 是 composables/useFolder.ts 中的 API 封装
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
