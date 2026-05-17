// ============================================================
// composables/useAuth.ts — 认证业务逻辑
// 封装登录/注册/刷新/退出操作，调用 useApi 发请求 + useAuthStore 保存状态
//
// 分层设计：
// useApi()    → 网络层（只管发请求、加token、解析响应）
// useAuth()   → 业务层（编排多个 API 调用 + 更新 store）
// useAuthStore → 状态层（只管存储和读取）
// ============================================================

import type { LoginDTO, LoginVO, RegisterDTO } from '~/types'

export function useAuth() {
  const store = useAuthStore()  // Pinia 认证状态
  const api = useApi()          // API 请求工具

  /** 用户名+密码登录，成功后将令牌保存到 store */
  async function login(credentials: LoginDTO): Promise<LoginVO> {
    // auth: false → 登录接口不需要认证头（用户还没令牌）
    const result = await api.post<LoginVO>('/api/auth/login', credentials, false)
    store.setSession(result)
    return result
  }

  /** 注册新用户，成功后自动登录（保存令牌） */
  async function register(data: RegisterDTO): Promise<LoginVO> {
    const result = await api.post<LoginVO>('/api/auth/register', data, false)
    store.setSession(result)
    return result
  }

  /** 用 refresh token 换新的 access token */
  async function refreshToken(): Promise<LoginVO> {
    const result = await api.post<LoginVO>(
      '/api/auth/refresh',
      { refreshToken: store.refreshToken },
      false,
    )
    store.setSession(result)
    return result
  }

  /** 退出登录，清除本地状态 */
  async function logout() {
    try {
      await api.post('/api/auth/logout', {})
    } catch {
      // 即使后端调用失败，也清除本地状态
    }
    store.clearSession()
  }

  return { login, register, refreshToken, logout }
}
