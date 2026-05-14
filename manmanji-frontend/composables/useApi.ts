import type { ApiResult } from '~/types'

function getStoredToken(): string | null {
  if (import.meta.client) {
    try {
      const saved = localStorage.getItem('mannote-auth')
      if (saved) return JSON.parse(saved).token
    } catch { /* ignore */ }
  }
  return null
}

export function useApi() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase || 'http://localhost:8080'

  async function request<T>(
    endpoint: string,
    options: {
      method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
      body?: unknown
      auth?: boolean
    } = {},
  ): Promise<T> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }

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

    const result: ApiResult<T> = await res.json()

    if (!res.ok || result.code !== 200) {
      throw new Error(result.message || `HTTP ${res.status}`)
    }

    return result.data
  }

  return {
    get: <T>(url: string, auth?: boolean) =>
      request<T>(url, { method: 'GET', auth }),
    post: <T>(url: string, body?: unknown, auth?: boolean) =>
      request<T>(url, { method: 'POST', body, auth }),
  }
}
