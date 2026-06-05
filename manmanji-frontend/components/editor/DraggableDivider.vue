<template>
  <div
    class="draggable-divider"
    @mousedown.prevent="startDrag"
    @dblclick="emit('update:splitRatio', 0.5)"
  >
    <div class="divider-handle" />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ splitRatio: number }>()
const emit = defineEmits<{ 'update:splitRatio': [value: number] }>()

/** Minimum width for the right panel (px) */
const MIN_RIGHT = 250

/**
 * Convert a 0-1 ratio to a pixel panel width using viewport-responsive scaling.
 * Only used as fallback when the slide panel DOM element is not available.
 */
function ratioToPx(ratio: number): number {
  const vw = window.innerWidth
  let base: number, factor: number, threshold: number
  if (vw <= 768)      { base = 160; factor = 40;  threshold = 772 }
  else if (vw <= 900) { base = 180; factor = 80;  threshold = 897 }
  else                { base = 200; factor = 160; threshold = 1092 }
  return base + factor * Math.min(ratio, 0.5)
    + Math.max(0, (vw - threshold) * Math.max(ratio - 0.5, 0) / 0.5)
}

function startDrag(e: MouseEvent) {
  const container = (e.currentTarget as HTMLElement).parentElement!
  const slidePanel = container.querySelector('.slide-panel') as HTMLElement | null
  const startX = e.clientX
  const startWidth = slidePanel?.offsetWidth ?? ratioToPx(props.splitRatio)
  // Max and min right-panel width: derived from the same viewport-responsive
  // formula used in EditorView CSS, so the right panel never squeezes the
  // editor card beyond its design-intended minimum.
  const maxWidth = ratioToPx(1)
  const minWidth = MIN_RIGHT
  let rafId = 0

  if (slidePanel) slidePanel.style.transition = 'none'
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'

  const onMove = (ev: MouseEvent) => {
    const width = Math.max(minWidth, Math.min(maxWidth, startWidth + startX - ev.clientX))
    if (!rafId) {
      rafId = requestAnimationFrame(() => {
        rafId = 0
        container.style.setProperty('--panel-px', width + 'px')
      })
    }
  }

  const onUp = () => {
    window.removeEventListener('mousemove', onMove)
    window.removeEventListener('mouseup', onUp)
    if (rafId) cancelAnimationFrame(rafId)

    if (slidePanel) slidePanel.style.transition = ''
    document.body.style.cursor = ''
    document.body.style.userSelect = ''

    const finalPx = parseFloat(container.style.getPropertyValue('--panel-px')) || startWidth
    emit('update:splitRatio', Math.max(0, Math.min(1, finalPx / (maxWidth + MIN_LEFT))))
  }

  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup', onUp)
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
  background: transparent;
  transform: translateZ(0);
}

.divider-handle {
  width: 5px;
  height: 24px;
  border-radius: 999px;
  background: var(--muted);
  opacity: 0.25;
  transition: opacity 0.2s var(--ease), background 0.2s var(--ease);
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
