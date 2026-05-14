import type { LoginDTO, LoginVO, RegisterDTO } from '~/types'

export function useAuth() {
  const store = useAuthStore()
  const api = useApi()

  async function login(credentials: LoginDTO): Promise<LoginVO> {
    const result = await api.post<LoginVO>('/api/auth/login', credentials, false)
    store.setSession(result)
    return result
  }

  async function register(data: RegisterDTO): Promise<LoginVO> {
    const result = await api.post<LoginVO>('/api/auth/register', data, false)
    store.setSession(result)
    return result
  }

  async function refreshToken(): Promise<LoginVO> {
    const result = await api.post<LoginVO>(
      '/api/auth/refresh',
      { refreshToken: store.refreshToken },
      false,
    )
    store.setSession(result)
    return result
  }

  function logout() {
    store.clearSession()
  }

  return { login, register, refreshToken, logout }
}
