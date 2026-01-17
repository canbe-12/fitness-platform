import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface Task {
  id: number
  taskDate: string // yyyy-MM-dd
  title: string
  description?: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
  priority: number
}

export interface TaskUpsertReq {
  taskDate: string
  title: string
  description?: string
  status?: string
  priority?: number
}

export function apiGetTasks(date: string) {
  return http.get<ApiEnvelope<Task[]>>('/api/tasks', { params: { date } })
}

export function apiGetTasksRange(from: string, to: string) {
  return http.get<ApiEnvelope<Task[]>>('/api/tasks/range', { params: { from, to } })
}

export function apiCreateTask(payload: TaskUpsertReq) {
  return http.post<ApiEnvelope<Task>>('/api/tasks', payload)
}

export function apiUpdateTask(id: number, payload: TaskUpsertReq) {
  return http.put<ApiEnvelope<Task>>(`/api/tasks/${id}`, payload)
}

export function apiDeleteTask(id: number) {
  return http.delete<ApiEnvelope<null>>(`/api/tasks/${id}`)
}

export function apiUpdateTaskStatus(id: number, status: string) {
  return http.patch<ApiEnvelope<Task>>(`/api/tasks/${id}/status`, null, { params: { status } })
}
