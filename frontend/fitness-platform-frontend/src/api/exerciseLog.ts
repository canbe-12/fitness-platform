import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface ExerciseSetReq {
  reps: number
  weightKg: number
}

export interface CreateExerciseLogReq {
  clientRequestId: string
  date: string // YYYY-MM-DD
  exerciseName: string
  muscleGroup?: string
  sets: ExerciseSetReq[]
  note?: string
}

export interface ExerciseLogResp {
  id: number
  date: string
  exerciseName: string
  muscleGroup?: string
  sets: ExerciseSetReq[]
  note?: string
}

export function apiCreateExerciseLog(payload: CreateExerciseLogReq) {
  return http.post<ApiEnvelope<{ id: number; date: string }>>('/api/exercise-logs', payload)
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
