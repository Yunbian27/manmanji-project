<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="context-menu-overlay"
      @click.self="emitClose"
      @contextmenu.prevent="emitClose"
    >
      <div
        ref="menuRef"
        class="context-menu"
        :style="menuStyle"
      >
        <button
          v-for="item in items"
          :key="item.key"
          class="context-menu-item"
          :class="{ 'context-menu-item--danger': item.danger }"
          @click="emitSelect(item.key)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
export interface MenuItem {
  key: string
  label: string
  danger?: boolean
}

const props = defineProps<{
  visible: boolean
  items: MenuItem[]
  position: { x: number; y: number }
}>()

const emit = defineEmits<{
  select: [key: string]
  close: []
}>()

const menuRef = ref<HTMLElement | null>(null)

const menuStyle = computed(() => {
  const padding = 8
  let left = props.position.x
  let top = props.position.y

  if (import.meta.client) {
    const menuWidth = 180
    const menuHeight = props.items.length * 36 + padding * 2
    if (left + menuWidth > window.innerWidth) {
      left = window.innerWidth - menuWidth - 8
    }
    if (top + menuHeight > window.innerHeight) {
      top = window.innerHeight - menuHeight - 8
    }
  }

  return {
    left: `${left}px`,
    top: `${top}px`,
  }
})

function emitSelect(key: string) {
  emit('select', key)
  emit('close')
}

function emitClose() {
  emit('close')
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    emitClose()
  }
}

watch(() => props.visible, (v) => {
  if (v) {
    nextTick(() => menuRef.value?.focus())
    window.addEventListener('keydown', onKeydown)
  } else {
    window.removeEventListener('keydown', onKeydown)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.context-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
}

.context-menu {
  position: fixed;
  z-index: 10000;
  min-width: 160px;
  padding: 8px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2);
  outline: none;
}

.context-menu-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: var(--radius-pill);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
  line-height: var(--leading-normal);
  text-align: left;
  cursor: pointer;
  transition: var(--transition-hover);
  white-space: nowrap;
}

.context-menu-item:hover {
  background: var(--canvas-soft);
}

.context-menu-item--danger {
  color: #c0392b;
}

.context-menu-item--danger:hover {
  background: #fde8e8;
}
</style>
