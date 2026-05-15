<!--
  pages/write.vue — Markdown 文章编辑页
  Nuxt 路由：/write
  渲染全屏编辑器（左侧竖排工具栏 + 编辑/预览区），TopNav 由 default layout 提供
  关闭时 router.back() 回到上一页
-->
<template>
  <div class="write-page">
    <EditorView @close="handleClose" @published="handlePublished" />
  </div>
</template>

<script setup lang="ts">
const router = useRouter()

function handleClose() {
  router.back()
}

function handlePublished(_articleId: number) {
  const folderStore = useFolderStore()
  folderStore.fetchFolders()
  router.back()
}
</script>

<style scoped>
.write-page {
  min-height: 100vh;
  background: var(--canvas);
}
</style>
