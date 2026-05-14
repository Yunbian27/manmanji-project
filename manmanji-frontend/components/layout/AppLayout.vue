<template>
  <div class="app-layout">
    <slot name="left-sidebar" />
    <main class="main-content content-area">
      <slot />
    </main>
    <slot name="right-sidebar" />
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: calc(100vh - var(--nav-height));
}

.main-content {
  flex: 0 0 auto;
  max-width: var(--content-max);
  width: 100%;
  padding: var(--space-xxl) 56px;
  overflow-y: auto;
  height: calc(100vh - var(--nav-height));
}

@media (max-width: 1200px) {
  .main-content {
    max-width: 860px;
    padding: var(--space-xl) 40px;
  }
}
@media (max-width: 1024px) {
  .main-content {
    max-width: 100%;
    padding: var(--space-xl);
  }
}
@media (max-width: 768px) {
  .main-content {
    padding: var(--space-lg) 20px;
  }
}
</style>
