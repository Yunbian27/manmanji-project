<!--
  AppButton.vue — 统一按钮组件
  四种变体覆盖项目所有按钮场景：
  - primary: 黄色实心（主要操作）
  - secondary: 深色描边（次要操作）
  - text: 纯文字（轻量操作）
  - icon: 圆形图标按钮

  使用示例：
  <AppButton variant="primary">注册</AppButton>
  <AppButton variant="icon"><IconHeart /></AppButton>
-->
<template>
  <button
    :class="['app-btn', `app-btn--${variant}`]"
    :disabled="disabled"
    type="button"
    @click="$emit('click')"
  >
    <!-- slot: Vue 的插槽机制，父组件传入的内容渲染在这里 -->
    <slot />
  </button>
</template>

<script setup lang="ts">
// defineProps: 声明组件接收的属性（props），TypeScript 泛型提供类型检查
defineProps<{
  variant?: 'primary' | 'secondary' | 'text' | 'icon'
  disabled?: boolean
}>()

// defineEmits: 声明组件发出的事件（emits），类似 DOM 的 addEventListener
defineEmits<{ click: [] }>()
</script>

<style scoped>
/* scoped: Vue 的样式隔离，这些 CSS 只作用于当前组件 */

.app-btn {
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  border-radius: var(--radius-md);        /* 8px — 按钮标准圆角 */
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  padding: 12px 20px;
  height: 40px;                           /* 设计规范要求的按钮高度 */
  transition: var(--transition-hover);    /* 150ms hover 过渡 */
}
.app-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 主按钮：黄色背景 + 黑色文字 */
.app-btn--primary {
  background: var(--primary);
  color: #0a0a0a;
}
.app-btn--primary:hover:not(:disabled) { background: var(--primary-active); }

/* 次按钮：深色卡片背景 + 细边框 */
.app-btn--secondary {
  background: var(--surface-card);
  color: var(--on-dark);
  border: 1px solid var(--hairline);
}
.app-btn--secondary:hover:not(:disabled) { background: var(--surface-elevated); }

/* 文字按钮：无背景，hover 变黄 */
.app-btn--text {
  background: transparent;
  color: var(--on-dark);
  padding: 12px 8px;
  height: auto;
}
.app-btn--text:hover:not(:disabled) { color: var(--primary); }

/* 图标按钮：36x36 圆形 */
.app-btn--icon {
  background: var(--surface-card);
  color: var(--on-dark);
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: var(--radius-full);
}
.app-btn--icon:hover:not(:disabled) { background: var(--surface-elevated); }
</style>
