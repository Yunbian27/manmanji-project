<!--
  app.vue — Nuxt 应用的根组件
  Nuxt 的入口点，所有页面都在 <NuxtPage /> 中渲染
  相当于 Spring Boot 的 main() 方法入口
-->
<template>
  <!-- NuxtLayout 读取页面对应的布局文件（默认 layouts/default.vue），将页面注入到布局的 <slot /> -->
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script setup lang="ts">
// useHead: Nuxt 的 SEO 工具，设置 <head> 标签内容
// 影响搜索引擎收录和浏览器标签页显示
useHead({
  title: '慢慢记 — 反内卷技术知识平台',
  meta: [
    { charset: 'utf-8' },
    { name: 'viewport', content: 'width=device-width, initial-scale=1' },
    { name: 'description', content: '慢慢记 — 个人博客 + 技术社区 + AI 对话助手' },
  ],
  htmlAttrs: { lang: 'zh-CN' },
})
</script>
