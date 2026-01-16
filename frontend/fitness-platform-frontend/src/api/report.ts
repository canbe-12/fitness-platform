import { http } from './http'
import type { ApiEnvelope } from '@/types/common'

export interface WeeklyReportResp {
  weekStart: string
  weekEnd: string
  workoutDays: number
  weightStartKg: number
  weightEndKg: number
}

export function apiGetWeeklyReport(weekStart: string) {
  return http.get<ApiEnvelope<WeeklyReportResp>>('/api/reports/weekly', {
    params: { weekStart },
  })
}
