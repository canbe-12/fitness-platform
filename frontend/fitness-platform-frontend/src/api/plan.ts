import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export type PlanTarget = 'FAT_LOSS' | 'MUSCLE_GAIN' | 'MAINTAIN'

export interface CurrentPlanResp {
  target: PlanTarget
  weeklyWorkoutDays: number
  dailyKcalTarget: number
  proteinTargetG: number
  createdAt: string
  updatedAt: string
}

export interface PlanUpsertReq {
  target: PlanTarget
  weeklyWorkoutDays: number
  dailyKcalTarget: number
  proteinTargetG: number
}

export function apiGetCurrentPlan() {
  return http.get<ApiEnvelope<CurrentPlanResp>>('/api/plans/current')
}

export function apiUpsertPlan(payload: PlanUpsertReq) {
  return http.put<ApiEnvelope<CurrentPlanResp>>('/api/plans/current', payload)
}
