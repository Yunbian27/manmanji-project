<!--
  pages/write.vue — Markdown 文章编辑页
  Nuxt 路由：/write
  使用 editor 布局（自定义顶部导航栏常驻 + 无 Footer）
-->
<template>
  <div class="write-page">
    <EditorView @close="handleClose" @published="handlePublished" />
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'editor' })

const router = useRouter()

function handleClose() {
  router.push('/home')
}

function handlePublished(_articleId: number) {
  const folderStore = useFolderStore()
  folderStore.fetchFolders()
  router.push('/home')
}
</script>

<style scoped>
.write-page {
  height: calc(100vh - var(--nav-height));
  background: var(--canvas);
}
</style>
