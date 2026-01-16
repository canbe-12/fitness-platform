import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

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

export interface DietLogCreateReq {
  clientRequestId: string
  date: string // YYYY-MM-DD
  mealType: MealType
  note?: string
  items: DietLogItemReq[]
}

export interface DietNutritionSnapshot {
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
  note?: string
  nutritionSnapshot?: DietNutritionSnapshot
  items: Array<
    DietLogItemReq & {
    id?: number
  }
  >
}

function normalizeToList<T>(data: any): T[] {
  if (!data) return []
  if (Array.isArray(data)) return data as T[]
  if (Array.isArray(data.records)) return data.records as T[]
  if (Array.isArray(data.list)) return data.list as T[]
  // 有些后端 day 直接返回单条
  if (typeof data === 'object') return [data as T]
  return []
}


export function apiCreateDietLog(payload: DietLogCreateReq) {
  return http.post<ApiEnvelope<DietLogResp>>('/api/diet-logs', payload)
}

export function apiUpdateDietLog(id: number, payload: Partial<DietLogCreateReq>) {
  return http.put<ApiEnvelope<DietLogResp>>(`/api/diet-logs/${id}`, payload)
}

export function apiGetDietLogById(id: number) {
  return http.get<ApiEnvelope<DietLogResp>>(`/api/diet-logs/${id}`)
}

export function apiDeleteDietLog(id: number) {
  return http.delete<ApiEnvelope<null>>(`/api/diet-logs/${id}`)
}

export async function apiGetDietLogsByDay(date: string) {
  const res = await http.get<ApiEnvelope<any>>('/api/diet-logs/day', { params: { date } })
  const list = normalizeToList<DietLogResp>(res.data.data)
  return { raw: res, list }
}

export async function apiGetDietLogsRange(params: { start: string; end: string }) {
  const res = await http.get<ApiEnvelope<any>>('/api/diet-logs/range', { params })
  const list = normalizeToList<DietLogResp>(res.data.data)
  return { raw: res, list }
}

export function apiGetNutritionDay(date: string) {
  return http.get<ApiEnvelope<DietNutritionSnapshot>>('/api/diet-logs/nutrition/day', {
    params: { date },
  })
}
