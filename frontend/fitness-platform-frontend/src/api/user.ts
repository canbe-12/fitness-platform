import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export interface MeResp {
  id: number
  username: string
  nickname?: string
  gender?: Gender
  birthday?: string
  heightCm?: number
  weightKg?: number
  goal?: string
  level?: string
}

export interface UpdateMeReq {
  nickname?: string
  gender?: Gender | string
  birthday?: string
  heightCm?: number
  weightKg?: number
  goal?: string
  level?: string
}

export function apiMe() {
  return http.get<ApiEnvelope<MeResp>>('/api/users/me')
}

export function apiUpdateMe(payload: UpdateMeReq) {
  return http.put<ApiEnvelope<MeResp>>('/api/users/me', payload)
}
