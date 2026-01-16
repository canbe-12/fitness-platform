import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface FoodResp {
  id: number
  foodName: string
  brand?: string
  unit?: string
  kcal: number
  proteinG: number
  carbG: number
  fatG: number
  fiberG?: number
  sodiumMg?: number
}

export interface CreateFoodReq {
  foodName: string
  brand?: string
  unit?: string
  kcal: number
  proteinG: number
  carbG: number
  fatG: number
  fiberG?: number
  sodiumMg?: number
}

export type FoodListData =
  | FoodResp[]
  | { records?: FoodResp[]; list?: FoodResp[]; total?: number }

export function apiGetFoods(params: {
  keyword?: string
  page?: number
  size?: number
}) {
  return http.get<ApiEnvelope<FoodListData>>('/api/foods', { params })
}

export function apiGetFoodById(id: number) {
  return http.get<ApiEnvelope<FoodResp>>(`/api/foods/${id}`)
}

export function apiCreateFood(payload: CreateFoodReq) {
  return http.post<ApiEnvelope<FoodResp>>('/api/foods', payload)
}

export function apiUpdateFood(id: number, payload: Partial<CreateFoodReq>) {
  return http.put<ApiEnvelope<FoodResp>>(`/api/foods/${id}`, payload)
}

export function apiDeleteFood(id: number) {
  return http.delete<ApiEnvelope<null>>(`/api/foods/${id}`)
}
