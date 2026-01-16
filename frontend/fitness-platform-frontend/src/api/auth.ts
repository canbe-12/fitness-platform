import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface LoginReq {
  username: string
  password: string
}

export interface LoginResp {
  accessToken: string
  expiresIn: number
}

export interface RegisterReq {
  username: string
  password: string
  nickname?: string
}

export function apiLogin(data: LoginReq) {
  return http.post<ApiEnvelope<LoginResp>>('/api/auth/login', data)
}

export function apiRegister(data: RegisterReq) {
  return http.post<ApiEnvelope<null>>('/api/auth/register', data)
}

export function apiLogout() {
  return http.post<ApiEnvelope<null>>('/api/auth/logout')
}
