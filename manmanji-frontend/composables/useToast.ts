export interface ToastItem {
  id: number
  message: string
  type: 'success' | 'error'
}

const TOAST_KEY = Symbol('toast')

let nextId = 0

export function createToastState() {
  const toasts = ref<ToastItem[]>([])

  function show(message: string, type: ToastItem['type'] = 'success') {
    const id = ++nextId
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, 3000)
  }

  return { toasts, show }
}

export type ToastState = ReturnType<typeof createToastState>

export function provideToast() {
  const state = createToastState()
  provide(TOAST_KEY, state)
  return state
}

export function injectToast(): ToastState {
  const toast = inject<ToastState>(TOAST_KEY)
  if (!toast) throw new Error('Toast state not provided. Call provideToast() in the layout.')
  return toast
}
