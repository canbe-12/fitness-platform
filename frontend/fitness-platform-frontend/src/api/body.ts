import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface WeightTrendPoint {
  date: string // yyyy-MM-dd
  weightKg: number
}

export interface WeightUpsertReq {
  weightKg: number
}

export function apiUpsertWeight(date: string, payload: WeightUpsertReq) {
  return http.put<ApiEnvelope<null>>('/api/body/weight', payload, {
    params: { date },
  })
}

export function apiGetWeightTrend(from: string, to: string) {
  return http.get<ApiEnvelope<WeightTrendPoint[]>>('/api/body/weight/trend', {
    params: { from, to },
  })
}

