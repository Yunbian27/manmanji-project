<!--
  AppTag.vue — 标签组件
  两种变体：
  - default: 深色药丸标签，用于普通文章标签
  - yellow: 黄色药丸标签，用于第一个重点标签（全大写）

  使用示例：
  <AppTag variant="default">Java</AppTag>
  <AppTag variant="yellow">原创</AppTag>
-->
<template>
  <span :class="['app-tag', variant === 'yellow' ? 'app-tag--yellow' : 'app-tag--default']">
    <slot />
  </span>
</template>

<script setup lang="ts">
// withDefaults: 给可选 prop 提供默认值
withDefaults(defineProps<{ variant?: 'default' | 'yellow' }>(), { variant: 'default' })
</script>

<style scoped>
.app-tag {
  font-family: var(--font-sans);
  padding: 4px 12px;
  border-radius: var(--radius-pill);      /* 9999px — 药丸形状 */
  transition: var(--transition-theme);
}

/* 默认标签：深色底 + 正文色 */
.app-tag--default {
  background: var(--surface-card);
  color: var(--body);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
}

/* 黄色标签：品牌黄底 + 黑色字 + 全大写 + 加宽字间距 */
.app-tag--yellow {
  background: var(--primary);
  color: #0a0a0a;
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  letter-spacing: var(--tracking-wide);   /* 1.5px 字间距 */
  text-transform: uppercase;              /* 强制全大写 */
}
</style>
