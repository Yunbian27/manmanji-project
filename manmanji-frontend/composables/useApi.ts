// ============================================================
// composables/useApi.ts — 基础 API 请求封装
// 所有 HTTP 请求的统一入口，自动处理 JWT 认证和错误
//
// Vue 3 Composable（组合式函数）：
// 以 use 开头命名的函数，内部使用 ref/reactive 等响应式 API
// 可以理解为"有状态的工具函数"，复用逻辑的同时复用状态
// ============================================================

import type { ApiResult } from '~/types'

/**
 * 从 localStorage 读取 JWT token
 * 放在函数外部避免循环依赖（不直接 import useAuthStore）
 * SSR 时返回 null（localStorage 只在浏览器端存在）
 */
function getStoredToken(): string | null {
  if (import.meta.client) {
    try {
      const saved = localStorage.getItem('mannote-auth')
      if (saved) return JSON.parse(saved).token
    } catch { /* JSON 解析失败则忽略 */ }
  }
  return null
}

export function useApi() {
  // useRuntimeConfig() 读取 nuxt.config.ts 中的 runtimeConfig
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase || 'http://localhost:8080'

  /**
   * 核心请求方法
   * @param endpoint API 路径，如 '/api/articles/1'
   * @param options  请求配置
   * @returns 解析后的响应数据（已提取 data 字段）
   */
  async function request<T>(
    endpoint: string,
    options: {
      method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
      body?: unknown           // unknown 比 any 更安全，使用前必须类型检查
      auth?: boolean           // false 表示跳过 JWT 注入（注册/登录时）
    } = {},
  ): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }

    // 默认需要认证，除非显式传 auth: false
    if (options.auth !== false) {
      const token = getStoredToken()
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }
    }

    const url = `${baseURL}${endpoint}`

    const res = await fetch(url, {
      method: options.method || 'GET',
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
    })

    // 解析后端统一响应 { code, message, data }
    const result: ApiResult<T> = await res.json()

    // code !== 200 视为业务错误
    if (!res.ok || result.code !== 200) {
      throw new Error(result.message || `HTTP ${res.status}`)
    }

    return result.data  // 只返回 data 字段
  }

  // 暴露简化的 get/post 方法
  return {
    get: <T>(url: string, auth?: boolean) =>
      request<T>(url, { method: 'GET', auth }),
    post: <T>(url: string, body?: unknown, auth?: boolean) =>
      request<T>(url, { method: 'POST', body, auth }),
  }
}
