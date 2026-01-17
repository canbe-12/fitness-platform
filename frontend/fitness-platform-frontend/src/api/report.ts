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

export interface DailyTrainingReport {
  date: string
  durationMinutes: number
  caloriesBurned: number
  setCount: number
  volume: number
  bodyParts: string[]
  exerciseNames: string[]
}

export interface DailyDietReport {
  date: string
  kcal: number
  protein: number
  carb: number
  fat: number
  mealCount: number
  mealKcalBreakdown: Record<string, number>
}

export interface ReportSummary {
  totalWorkouts: number
  totalDurationMinutes: number
  totalTrainingKcal: number
  avgDailyKcalIntake: number
  avgProtein: number
  avgCarb: number
  avgFat: number
}

export interface ComprehensiveReportResp {
  start: string
  end: string
  trainingHistory: DailyTrainingReport[]
  dietHistory: DailyDietReport[]
  summary: ReportSummary
}

export function apiGetComprehensiveReport(start: string, end: string) {
  return http.get<ApiEnvelope<ComprehensiveReportResp>>('/api/reports/comprehensive', {
    params: { start, end },
  })
}
