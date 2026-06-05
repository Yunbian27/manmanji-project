<template>
  <div
    class="draggable-divider"
    @mousedown.prevent="startDrag"
    @dblclick="emit('update:splitRatio', 0.5)"
  >
    <div class="divider-line" />
    <div class="divider-handle">
      <svg width="4" height="16" viewBox="0 0 4 16" fill="none">
        <rect x="0" y="1" width="1" height="14" rx="0.5" />
        <rect x="3" y="1" width="1" height="14" rx="0.5" />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ splitRatio: number }>()
const emit = defineEmits<{ 'update:splitRatio': [value: number] }>()

function startDrag(e: MouseEvent) {
  const startX = e.clientX
  const startRatio = props.splitRatio
  const container = (e.target as HTMLElement).parentElement!
  const containerWidth = container.offsetWidth

  function onMove(ev: MouseEvent) {
    const dx = ev.clientX - startX
    let newRatio = startRatio + dx / containerWidth
    if (newRatio < 0.05) newRatio = 0
    else if (newRatio > 0.95) newRatio = 1
    else newRatio = Math.min(0.95, Math.max(0.05, newRatio))
    emit('update:splitRatio', newRatio)
  }

  function onUp() {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}
</script>

<style scoped>
.draggable-divider {
  width: 16px;
  flex-shrink: 0;
  cursor: col-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  background: transparent;
}

.divider-line {
  display: none;
}

.divider-handle {
  width: 5px;
  height: 24px;
  border-radius: 999px;
  background: var(--muted);
  opacity: 0.25;
  transition: opacity 0.2s var(--ease), background 0.2s var(--ease);
}

.divider-handle svg {
  display: none;
}

.draggable-divider:hover .divider-handle {
  opacity: 0.7;
  background: var(--slate);
}

.draggable-divider:active .divider-handle {
  opacity: 1;
  background: var(--primary);
}
</style>
