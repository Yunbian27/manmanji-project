import type { ArticleCreateDTO } from '~/types'

export interface PublishSettings {
  categoryId: number | null
  tagIds: number[]
  coverUrl: string
  summary: string
  isOriginal: boolean
  sourceUrl: string
}

const EDITOR_KEY = Symbol('editor')

export function createEditorState() {
  const title = ref('')
  const content = ref('')
  const viewMode = ref<'edit' | 'split' | 'preview'>('split')
  const publishSettingsOpen = ref(false)
  const currentArticleId = ref<number | null>(null)
  const isSaving = ref(false)
  const lastSavedAt = ref<string | null>(null)
  const publishError = ref<string | null>(null)
  const titleError = ref<string | null>(null)

  const publishSettings = reactive<PublishSettings>({
    categoryId: null,
    tagIds: [],
    coverUrl: '',
    summary: '',
    isOriginal: true,
    sourceUrl: '',
  })

  const draftKey = computed(() => {
    const t = title.value.trim()
    return t ? `mannote-draft-${t}` : 'mannote-draft-__new__'
  })

  let autoSaveTimer: ReturnType<typeof setTimeout> | null = null

  function saveDraft() {
    if (!content.value && !title.value) return
    const draft = {
      title: title.value,
      content: content.value,
      settings: { ...publishSettings },
      articleId: currentArticleId.value,
      savedAt: new Date().toISOString(),
    }
    localStorage.setItem(draftKey.value, JSON.stringify(draft))
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  function loadDraft() {
    const key = Object.keys(localStorage).find(k => k.startsWith('mannote-draft-'))
    if (!key) return
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return
      const draft = JSON.parse(raw)
      title.value = draft.title || ''
      content.value = draft.content || ''
      if (draft.settings) {
        publishSettings.categoryId = draft.settings.categoryId ?? null
        publishSettings.tagIds = draft.settings.tagIds ?? []
        publishSettings.coverUrl = draft.settings.coverUrl ?? ''
        publishSettings.summary = draft.settings.summary ?? ''
        publishSettings.isOriginal = draft.settings.isOriginal ?? true
        publishSettings.sourceUrl = draft.settings.sourceUrl ?? ''
      }
      currentArticleId.value = draft.articleId ?? null
    } catch {
      // corrupted draft, ignore
    }
  }

  function clearDraft() {
    localStorage.removeItem(draftKey.value)
  }

  function startAutoSave() {
    watch([content, title], () => {
      if (autoSaveTimer) clearTimeout(autoSaveTimer)
      autoSaveTimer = setTimeout(() => {
        if (content.value || title.value) {
          saveDraft()
        }
      }, 30000)
    }, { immediate: false })
  }

  function stopAutoSave() {
    if (autoSaveTimer) {
      clearTimeout(autoSaveTimer)
      autoSaveTimer = null
    }
  }

  function reset() {
    title.value = ''
    content.value = ''
    viewMode.value = 'split'
    publishSettingsOpen.value = false
    currentArticleId.value = null
    isSaving.value = false
    lastSavedAt.value = null
    publishError.value = null
    titleError.value = null
    publishSettings.categoryId = null
    publishSettings.tagIds = []
    publishSettings.coverUrl = ''
    publishSettings.summary = ''
    publishSettings.isOriginal = true
    publishSettings.sourceUrl = ''
  }

  function buildDTO(): ArticleCreateDTO {
    return {
      title: title.value.trim(),
      content: content.value,
      summary: publishSettings.summary || undefined,
      coverUrl: publishSettings.coverUrl || undefined,
      categoryId: publishSettings.categoryId || undefined,
      tagIds: publishSettings.tagIds.length > 0 ? publishSettings.tagIds : undefined,
      isOriginal: publishSettings.isOriginal,
      sourceUrl: publishSettings.sourceUrl || undefined,
    }
  }

  function validate(): boolean {
    titleError.value = null
    publishError.value = null
    if (!title.value.trim()) {
      titleError.value = '请输入文章标题'
      return false
    }
    if (!content.value.trim()) {
      publishError.value = '请输入文章内容'
      return false
    }
    return true
  }

  async function publish(status: 'DRAFT' | 'PUBLISHED' = 'PUBLISHED'): Promise<number | null> {
    if (!validate()) return null
    isSaving.value = true
    publishError.value = null
    try {
      const { createArticle } = useArticle()
      const dto = buildDTO()
      dto.status = status
      const id = await createArticle(dto)
      clearDraft()
      return id
    } catch (e: any) {
      publishError.value = e?.message || '发布失败，请稍后重试'
      return null
    } finally {
      isSaving.value = false
    }
  }

  return {
    title,
    content,
    viewMode,
    publishSettingsOpen,
    currentArticleId,
    isSaving,
    lastSavedAt,
    publishError,
    titleError,
    publishSettings,
    draftKey,
    saveDraft,
    loadDraft,
    clearDraft,
    startAutoSave,
    stopAutoSave,
    reset,
    buildDTO,
    validate,
    publish,
  }
}

export type EditorState = ReturnType<typeof createEditorState>

export function provideEditor(editor: EditorState) {
  provide(EDITOR_KEY, editor)
}

export function injectEditor(): EditorState {
  const editor = inject<EditorState>(EDITOR_KEY)
  if (!editor) throw new Error('Editor state not provided. Did you forget to call provideEditor()?')
  return editor
}
