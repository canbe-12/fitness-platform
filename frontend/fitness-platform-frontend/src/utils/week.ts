import { toYmd } from './date.ts'

export function getWeekStartMonday(d: Date): string {
  const date = new Date(d)
  const day = date.getDay() // 0 Sun, 1 Mon...
  const diff = (day === 0 ? -6 : 1 - day) // move to Monday
  date.setDate(date.getDate() + diff)
  return toYmd(date)
}
