import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

export interface NutritionDayResp {
  kcal: number
  proteinG: number
  carbG: number
  fatG: number
  fiberG: number
  sodiumMg: number
}

export interface DietLogItemReq {
  foodId?: number
  foodName: string
  brand?: string
  amount: number
  unit: string
  kcal: number
  proteinG: number
  carbG: number
  fatG: number
  fiberG?: number
  sodiumMg?: number
}

export interface CreateDietLogReq {
  clientRequestId: string
  date: string
  mealType: MealType
  items: DietLogItemReq[]
}

export interface CreateDietLogResp {
  id: number
  date: string
}

export function apiGetNutritionDay(date: string) {
  return http.get<ApiEnvelope<NutritionDayResp>>('/api/diet-logs/nutrition/day', {
    params: { date },
  })
}

export function apiCreateDietLog(payload: CreateDietLogReq) {
  return http.post<ApiEnvelope<CreateDietLogResp>>('/api/diet-logs', payload)
}

export interface DietLogItemResp {
  id?: number
  foodId?: number
  foodName: string
  brand?: string
  amount: number
  unit: string
  kcal: number
  proteinG: number
  carbG: number
  fatG: number
  fiberG?: number
  sodiumMg?: number
}

export interface DietLogResp {
  id: number
  date: string
  mealType: MealType
  items: DietLogItemResp[]
}

export interface DietLogRangeItem {
  id: number
  date: string
  mealType: MealType
  kcal: number
  proteinG: number
  carbG: number
  fatG: number
}

export interface UpdateDietLogReq {
  date?: string
  mealType?: MealType
  items?: DietLogItemReq[]
}

export function apiGetDietLogsByDay(date: string) {
  return http.get<ApiEnvelope<DietLogResp[]>>('/api/diet-logs/day', {
    params: { date },
  })
}

export function apiGetDietLogsRange(startDate: string, endDate: string) {
  return http.get<ApiEnvelope<DietLogRangeItem[]>>('/api/diet-logs/range', {
    params: { startDate, endDate },
  })
}

export function apiGetDietLogById(id: number) {
  return http.get<ApiEnvelope<DietLogResp>>(`/api/diet-logs/${id}`)
}

export function apiUpdateDietLog(id: number, payload: UpdateDietLogReq) {
  return http.put<ApiEnvelope<DietLogResp>>(`/api/diet-logs/${id}`, payload)
}

export function apiDeleteDietLog(id: number) {
  return http.delete<ApiEnvelope<null>>(`/api/diet-logs/${id}`)
}
