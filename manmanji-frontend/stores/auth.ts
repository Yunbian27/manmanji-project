import { defineStore } from 'pinia'
import type { UserInfo, LoginVO } from '~/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const user = ref<UserInfo | null>(null)
  const expiresAt = ref<number>(0)

  function setSession(data: LoginVO) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user
    expiresAt.value = Date.now() + data.expiresIn * 1000
    persist()
  }

  function clearSession() {
    token.value = null
    refreshToken.value = null
    user.value = null
    expiresAt.value = 0
    persist()
  }

  const isAuthenticated = computed(
    () => !!token.value && Date.now() < expiresAt.value,
  )
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function persist() {
    if (import.meta.client) {
      localStorage.setItem(
        'mannote-auth',
        JSON.stringify({
          token: token.value,
          refreshToken: refreshToken.value,
          user: user.value,
          expiresAt: expiresAt.value,
        }),
      )
    }
  }

  function hydrate() {
    if (import.meta.client) {
      try {
        const saved = localStorage.getItem('mannote-auth')
        if (saved) {
          const data = JSON.parse(saved)
          token.value = data.token
          refreshToken.value = data.refreshToken
          user.value = data.user
          expiresAt.value = data.expiresAt
        }
      } catch {
        /* ignore corrupt data */
      }
    }
  }

  return {
    token,
    refreshToken,
    user,
    expiresAt,
    isAuthenticated,
    isAdmin,
    setSession,
    clearSession,
    hydrate,
  }
})
