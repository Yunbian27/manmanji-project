<template>
  <button
    class="theme-toggle"
    :aria-label="isDark ? '切换到浅色主题' : '切换到深色主题'"
    @click="toggleTheme"
  >
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
const { isDark, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  flex-shrink: 0;
  background: var(--primary);
  color: #0a0a0a;
  transition: background 0.3s ease, color 0.3s ease, transform 0.3s ease;
  position: relative;
}

[data-theme="light"] .theme-toggle {
  background: #0a0a0a;
  color: var(--primary);
}

.theme-toggle:hover {
  background: var(--primary-active);
  transform: scale(1.08);
}

[data-theme="light"] .theme-toggle:hover {
  background: #1a1a1a;
}

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

.theme-icon-sun,
.theme-icon-moon {
  position: absolute;
  top: 0;
  left: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.theme-icon-sun.hidden {
  opacity: 0;
  transform: rotate(90deg);
}

.theme-icon-moon.hidden {
  opacity: 0;
  transform: rotate(-90deg);
}
</style>
