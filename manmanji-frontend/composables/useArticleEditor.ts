import type { ArticleSaveDTO, ArticlePublishDTO } from '~/types'

export interface PublishSettings {
  categoryId: number | null
  tagIds: number[]
  coverUrl: string
  summary: string
  isOriginal: boolean
  sourceUrl: string
}

const EDITOR_KEY = Symbol('editor')

export function createEditorState(articleId: number) {
  const title = ref('')
  const content = ref('')
  const viewMode = ref<'edit' | 'split' | 'preview'>('split')
  const publishSettingsOpen = ref(false)
  const currentArticleId = ref<number>(articleId)
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

  async function loadFromServer() {
    try {
      const { getArticle } = useArticle()
      const article = await getArticle(currentArticleId.value)
      title.value = article.title || ''
      content.value = article.content || ''
      publishSettings.categoryId = article.categoryId ?? null
      publishSettings.coverUrl = article.coverUrl ?? ''
      publishSettings.summary = article.summary ?? ''
      publishSettings.isOriginal = article.isOriginal ?? true
      publishSettings.sourceUrl = article.sourceUrl ?? ''
    } catch {
      // article not found or network error, stay with empty state
    }
  }

  function reset() {
    title.value = ''
    content.value = ''
    viewMode.value = 'split'
    publishSettingsOpen.value = false
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

  async function save(): Promise<void> {
    if (!validate()) return
    isSaving.value = true
    publishError.value = null
    try {
      const { saveArticle } = useArticle()
      const id = await saveArticle({
        id: currentArticleId.value,
        title: title.value.trim(),
        content: content.value,
      })
      if (currentArticleId.value === 0) {
        currentArticleId.value = id
      }
      lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } catch (e: any) {
      publishError.value = e?.message || '保存失败，请稍后重试'
    } finally {
      isSaving.value = false
    }
  }

  async function publish() {
    if (!validate()) return
    isSaving.value = true
    publishError.value = null
    try {
      const { publishArticle } = useArticle()
      const id = await publishArticle({
        id: currentArticleId.value,
        title: title.value.trim(),
        content: content.value,
        summary: publishSettings.summary || undefined,
        coverUrl: publishSettings.coverUrl || undefined,
        categoryId: publishSettings.categoryId || undefined,
        tagIds: publishSettings.tagIds.length > 0 ? publishSettings.tagIds : undefined,
        isOriginal: publishSettings.isOriginal,
        sourceUrl: publishSettings.sourceUrl || undefined,
      })
      if (currentArticleId.value === 0) {
        currentArticleId.value = id
      }
      lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } catch (e: any) {
      publishError.value = e?.message || '发布失败，请稍后重试'
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
    loadFromServer,
    reset,
    validate,
    save,
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
