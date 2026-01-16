import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface ExerciseResp {
  id: number
  name: string
  muscleGroup?: string
  equipment?: string
  defaultTargetWeight?: number
  defaultTargetReps?: number
}

export interface ExercisePageResp {
  content: ExerciseResp[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export function apiSearchExercises(keyword?: string, muscleGroup?: string, page = 0, size = 20) {
  return http.get<ApiEnvelope<ExercisePageResp>>('/api/exercises', {
    params: { keyword, muscleGroup, page, size },
  })
}

export function apiGetExercise(id: number) {
  return http.get<ApiEnvelope<ExerciseResp>>(`/api/exercises/${id}`)
}

