// ============================================================
// stores/auth.ts — 用户认证状态管理（Pinia Store）
// 管理 JWT 令牌和用户信息，持久化到 localStorage
//
// 认证流程：
// 1. 用户登录 → setSession() 保存令牌
// 2. API 请求 → useApi() 读取 token 加到 Authorization 头
// 3. 令牌过期 → refreshToken() 用刷新令牌换新令牌
// 4. 用户退出 → clearSession() 清除所有数据
// ============================================================

import { defineStore } from 'pinia'
import type { UserInfo, LoginVO } from '~/types'

export const useAuthStore = defineStore('auth', () => {
  // ---- 状态（state）----
  const token = ref<string | null>(null)           // JWT access token
  const refreshToken = ref<string | null>(null)    // JWT refresh token
  const user = ref<UserInfo | null>(null)          // 当前用户信息
  const expiresAt = ref<number>(0)                 // 令牌过期时间（毫秒时间戳）

  const COOKIE_NAME = 'mannote-token'
  const COOKIE_MAX_AGE = 60 * 60 * 24 * 7 // 7 天

  function setCookie(value: string) {
    if (import.meta.client) {
      document.cookie = `${COOKIE_NAME}=${value}; path=/; max-age=${COOKIE_MAX_AGE}; SameSite=Lax`
    }
  }

  function clearCookie() {
    if (import.meta.client) {
      document.cookie = `${COOKIE_NAME}=; path=/; max-age=0`
    }
  }

  /** 保存登录会话（注册/登录/刷新后调用） */
  function setSession(data: LoginVO) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user
    expiresAt.value = Date.now() + data.expiresIn * 1000
    setCookie(data.accessToken)       // SSR 认证检查依赖 cookie
    persist()
  }

  /** 清除登录会话（退出登录时调用） */
  function clearSession() {
    token.value = null
    refreshToken.value = null
    user.value = null
    expiresAt.value = 0
    clearCookie()
    persist()
  }

  /** 将状态序列化到 localStorage（仅客户端） */
  function persist() {
    if (import.meta.client) {
      localStorage.setItem('mannote-auth', JSON.stringify({
        token: token.value,
        refreshToken: refreshToken.value,
        user: user.value,
        expiresAt: expiresAt.value,
      }))
    }
  }

  /** 从 localStorage 恢复状态（应用启动时调用） */
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
        // JSON 解析失败则忽略（数据损坏或首次访问）
      }
    }
  }

  // ---- 计算属性（getters）----
  /** 是否已登录：有令牌且未过期 */
  const isAuthenticated = computed(
    () => !!token.value && Date.now() < expiresAt.value,
  )

  /** 是否管理员 */
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  return {
    token, refreshToken, user, expiresAt,
    isAuthenticated, isAdmin,
    setSession, clearSession, hydrate,
  }
})
