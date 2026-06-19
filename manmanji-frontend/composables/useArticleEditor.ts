import type { ArticleSaveDTO, ArticlePublishDTO } from '~/types'

export interface PublishSettings {
  tagIds: number[]
  groupNames: string[]
  coverUrl: string
  articleType: 'ORIGINAL' | 'REPOST'
  summary: string
  sourceUrl: string
  visibility: 'PUBLIC' | 'PRIVATE' | 'FOLLOWER'
  creationStatement: 'NONE' | 'AI_ASSISTED' | 'NETWORK_SOURCED' | 'PERSONAL_OPINION'
}

export const EDITOR_KEY = Symbol('editor')

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
    tagIds: [],
    groupNames: [],
    coverUrl: '',
    articleType: 'ORIGINAL',
    summary: '',
    sourceUrl: '',
    visibility: 'PUBLIC',
    creationStatement: 'NONE',
  })

  async function loadFromServer() {
    try {
      const { getArticleForEditor } = useArticle()
      const article = await getArticleForEditor(currentArticleId.value)
      title.value = article.title || ''
      content.value = article.content || ''
      publishSettings.coverUrl = article.coverUrl ?? ''
      publishSettings.summary = article.summary ?? ''
      publishSettings.articleType = (article.articleType as PublishSettings['articleType']) ?? 'ORIGINAL'
      publishSettings.sourceUrl = article.sourceUrl ?? ''
      publishSettings.visibility = (article.visibility as PublishSettings['visibility']) ?? 'PUBLIC'
      publishSettings.creationStatement = (article.creationStatement as PublishSettings['creationStatement']) ?? 'NONE'
      publishSettings.tagIds = article.tagIds ?? []
      publishSettings.groupNames = article.groupNames ?? []
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
    publishSettings.tagIds = []
    publishSettings.groupNames = []
    publishSettings.coverUrl = ''
    publishSettings.articleType = 'ORIGINAL'
    publishSettings.summary = ''
    publishSettings.sourceUrl = ''
    publishSettings.visibility = 'PUBLIC'
    publishSettings.creationStatement = 'NONE'
  }

  function validate(): boolean {
    titleError.value = null
    publishError.value = null
    if (!title.value.trim()) {
      titleError.value = '请输入文章标题'
      return false
    }
    if (title.value.trim().length < 5) {
      titleError.value = '标题至少 5 个字'
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
        ...(currentArticleId.value > 0 ? { id: currentArticleId.value } : {}),
        title: title.value.trim(),
        content: content.value,
      } as ArticleSaveDTO)
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

  async function publish(): Promise<boolean> {
    if (!validate()) return false
    isSaving.value = true
    publishError.value = null
    try {
      const { publishArticle } = useArticle()
      const dto: ArticlePublishDTO = {
        ...(currentArticleId.value > 0 ? { id: currentArticleId.value } : {}),
        title: title.value.trim(),
        content: content.value,
        summary: publishSettings.summary || undefined,
        coverUrl: publishSettings.coverUrl || undefined,
        tagIds: publishSettings.tagIds.length > 0 ? publishSettings.tagIds : undefined,
        groupNames: publishSettings.groupNames.length > 0 ? publishSettings.groupNames : undefined,
        articleType: publishSettings.articleType,
        sourceUrl: publishSettings.sourceUrl || undefined,
        visibility: publishSettings.visibility,
        creationStatement: publishSettings.creationStatement !== 'NONE' ? publishSettings.creationStatement : undefined,
      }
      const id = await publishArticle(dto)
      if (currentArticleId.value === 0) {
        currentArticleId.value = id
      }
      lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      return true
    } catch (e: any) {
      publishError.value = e?.message || '发布失败，请稍后重试'
      return false
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
