// ============================================================
// composables/useApi.ts — 基础 API 请求封装
// 所有 HTTP 请求的统一入口，自动处理 JWT 认证和错误
//
// Vue 3 Composable（组合式函数）：
// 以 use 开头命名的函数，内部使用 ref/reactive 等响应式 API
// 可以理解为"有状态的工具函数"，复用逻辑的同时复用状态
// ============================================================

import type { ApiResult, LoginVO } from '~/types'

/**
 * 从 localStorage 读取 JWT token
 * SSR 时返回 null（localStorage 只在浏览器端存在）
 */
function getStoredToken(): string | null {
  if (import.meta.client) {
    try {
      const saved = localStorage.getItem('mannote-auth')
      if (saved) return JSON.parse(saved).token
    } catch { /* 忽略 */ }
  }
  return null
}

function getStoredRefreshToken(): string | null {
  if (import.meta.client) {
    try {
      const saved = localStorage.getItem('mannote-auth')
      if (saved) return JSON.parse(saved).refreshToken
    } catch { /* 忽略 */ }
  }
  return null
}

function saveSession(data: LoginVO) {
  if (!import.meta.client) return
  const old = JSON.parse(localStorage.getItem('mannote-auth') || '{}')
  localStorage.setItem('mannote-auth', JSON.stringify({
    ...old,
    token: data.accessToken,
    refreshToken: data.refreshToken,
    user: data.user,
    expiresAt: Date.now() + data.expiresIn * 1000,
  }))
}

function clearSession() {
  if (import.meta.client) {
    localStorage.removeItem('mannote-auth')
    document.cookie = 'mannote-token=; path=/; max-age=0'
  }
}

/** 防止多个请求同时触发刷新，共用同一个刷新 Promise */
let refreshPromise: Promise<boolean> | null = null

async function tryRefresh(baseURL: string): Promise<boolean> {
  // 已在刷新中，等它完成
  if (refreshPromise) return refreshPromise

  refreshPromise = (async () => {
    try {
      const refreshToken = getStoredRefreshToken()
      if (!refreshToken) return false

      const res = await fetch(`${baseURL}/api/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      })
      const result: ApiResult<LoginVO> = await res.json()
      if (!res.ok || result.code !== 200) return false

      saveSession(result.data)
      return true
    } catch {
      return false
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}

export function useApi() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase ?? 'http://localhost:8080'

  async function request<T>(
    endpoint: string,
    options: {
      method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
      body?: unknown
      auth?: boolean           // false = 跳过 JWT
    } = {},
  ): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }

    if (options.auth !== false) {
      const token = getStoredToken()
      if (token) headers['Authorization'] = `Bearer ${token}`
    }

    const url = `${baseURL}${endpoint}`

    let res = await fetch(url, {
      method: options.method || 'GET',
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
    })

    // 401 → 尝试刷新 token 后重试
    if (res.status === 401 && options.auth !== false) {
      const refreshed = await tryRefresh(baseURL)
      if (refreshed) {
        const newToken = getStoredToken()
        if (newToken) headers['Authorization'] = `Bearer ${newToken}`
        res = await fetch(url, {
          method: options.method || 'GET',
          headers,
          body: options.body ? JSON.stringify(options.body) : undefined,
        })
      } else {
        clearSession()
        if (import.meta.client) navigateTo('/login')
        throw new Error('登录已过期，请重新登录')
      }
    }

    const result: ApiResult<T> = await res.json()

    if (!res.ok || result.code !== 200) {
      throw new Error(result.message || `HTTP ${res.status}`)
    }

    return result.data
  }

  /**
   * Multipart 上传（不设 Content-Type，让浏览器自动设 multipart/form-data）
   */
  async function uploadFormData<T>(endpoint: string, formData: FormData, auth = true): Promise<T> {
    const headers: Record<string, string> = {}

    if (auth) {
      const token = getStoredToken()
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }
    }

    const url = `${baseURL}${endpoint}`
    const res = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
    })

    const result: ApiResult<T> = await res.json()
    if (!res.ok || result.code !== 200) {
      throw new Error(result.message || `HTTP ${res.status}`)
    }
    return result.data
  }

  // 暴露简化的 get/post 方法
  return {
    get: <T>(url: string, auth?: boolean) =>
      request<T>(url, { method: 'GET', auth }),
    post: <T>(url: string, body?: unknown, auth?: boolean) =>
      request<T>(url, { method: 'POST', body, auth }),
    put: <T>(url: string, body?: unknown, auth?: boolean) =>
      request<T>(url, { method: 'PUT', body, auth }),
    delete: <T>(url: string, auth?: boolean) =>
      request<T>(url, { method: 'DELETE', auth }),
    uploadFormData: <T>(endpoint: string, formData: FormData, auth?: boolean) =>
      uploadFormData<T>(endpoint, formData, auth),
  }
}
