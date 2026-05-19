<!--
  pages/write.vue — Markdown 文章编辑页
  Nuxt 路由：/write
  使用 editor 布局（自定义顶部导航栏常驻 + 无 Footer）
-->
<template>
  <div class="write-page">
    <EditorView v-if="articleId" :article-id="articleId" @close="handleClose" />
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'editor' })

const route = useRoute()
const router = useRouter()
const articleId = ref(0)

watch(() => route.query.articleId, (val) => {
  articleId.value = Number(val) || 0
}, { immediate: true })

function handleClose() {
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
