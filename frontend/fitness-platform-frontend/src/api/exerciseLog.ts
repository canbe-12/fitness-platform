import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface CreateExerciseLogReq {
  exerciseId: number
  plannedDate: string
  actualWeight: number
  actualReps: number
  plannedWeight?: number
  plannedReps?: number
  clientRequestId: string
  notes?: string
}

export interface ExerciseLogResp {
  id: number
  exerciseId: number
  plannedDate: string
  actualWeight: number
  actualReps: number
  plannedWeight?: number
  plannedReps?: number
  clientRequestId?: string
  notes?: string
  createdAt?: string
  updatedAt?: string
}

export function apiCreateExerciseLog(payload: CreateExerciseLogReq) {
  return http.post<ApiEnvelope<ExerciseLogResp>>('/api/exercise-logs', payload)
}

export function apiListExerciseLogs() {
  return http.get<ApiEnvelope<ExerciseLogResp[]>>('/api/exercise-logs')
}

export function apiRangeExerciseLogs(startDate: string, endDate: string) {
  return http.get<ApiEnvelope<ExerciseLogResp[]>>('/api/exercise-logs/range', {
    params: { startDate, endDate },
  })
}

export function apiDeleteExerciseLog(id: number) {
  return http.delete<ApiEnvelope<null>>(`/api/exercise-logs/${id}`)
}
