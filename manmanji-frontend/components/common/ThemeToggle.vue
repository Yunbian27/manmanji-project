<!--
  ThemeToggle.vue — 主题切换按钮
  40×40 方形按钮，太阳/月亮两个 SVG 图标叠加
  通过 opacity + transform 实现平滑切换动画

  设计规范（SPEC.md 4.3）：
  - 深色模式：黄色底 + 黑色太阳图标
  - 浅色模式：黑色底 + 黄色月亮图标
  - hover 背景加深 + scale(1.08)
-->
<template>
  <button
    class="theme-toggle"
    :aria-label="isDark ? '切换到浅色主题' : '切换到深色主题'"
    @click="toggleTheme"
  >
    <!-- 两个图标绝对定位叠在一起，通过 .hidden 类切换可见性 -->
    <span class="theme-icon-wrapper">
      <IconSun
        :size="18"
        :class="['theme-icon-sun', { hidden: !isDark }]"
      />
      <IconMoon
        :size="18"
        :class="['theme-icon-moon', { hidden: isDark }]"
      />
    </span>
  </button>
</template>

<script setup lang="ts">
// useTheme() 返回 isDark 和 toggleTheme，由 composables/useTheme.ts 提供
const { isDark, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  flex-shrink: 0;               /* 防止被父 flex 容器压缩 */
  background: var(--primary);   /* 深色模式：黄底 */
  color: #0a0a0a;               /* 黑字（图标色） */
  transition: background 0.3s ease, color 0.3s ease, transform 0.3s ease;
  position: relative;
}

/* 浅色模式：黑底黄字，翻转配色 */
[data-theme="light"] .theme-toggle {
  background: #0a0a0a;
  color: var(--primary);
}

.theme-toggle:hover {
  background: var(--primary-active);
  transform: scale(1.08);       /* 微放大 */
}
[data-theme="light"] .theme-toggle:hover {
  background: #1a1a1a;
}

/* 键盘焦点环（无障碍访问） */
.theme-toggle:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.theme-icon-wrapper {
  position: relative;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 两个图标绝对定位重叠，通过 opacity + rotate 渐变切换 */
.theme-icon-sun,
.theme-icon-moon {
  position: absolute;
  top: 0;
  left: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.theme-icon-sun.hidden { opacity: 0; transform: rotate(90deg); }
.theme-icon-moon.hidden { opacity: 0; transform: rotate(-90deg); }
</style>
