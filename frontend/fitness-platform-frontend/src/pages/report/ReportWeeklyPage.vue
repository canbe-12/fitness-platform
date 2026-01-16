<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">本周训练报告</div>

          <div class="actions">
            <el-date-picker
              v-model="weekStart"
              type="date"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
              placeholder="选择周起始日(周一)"
              style="width: 180px"
            />
            <el-button :loading="loading" @click="load" plain>刷新</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="4" animated />

      <template v-else>
        <el-alert
          v-if="!report"
          type="info"
          show-icon
          title="暂无周报数据"
          description="你可以先去“训练执行/饮食记录”提交一些数据，然后再回来刷新周报。"
        />

        <template v-else>
          <!-- 关键指标 -->
          <div class="kpi">
            <div class="kpi-item">
              <span>周区间</span>
              <b>{{ report.weekStart }} ~ {{ report.weekEnd }}</b>
            </div>
            <div class="kpi-item">
              <span>训练天数</span>
              <b>{{ report.workoutDays }} 天</b>
            </div>
            <div class="kpi-item">
              <span>周初体重</span>
              <b>{{ report.weightStartKg }} kg</b>
            </div>
            <div class="kpi-item">
              <span>周末体重</span>
              <b>{{ report.weightEndKg }} kg</b>
            </div>
          </div>

          <!-- 体重变化可视化 -->
          <el-card shadow="never" class="subcard">
            <div class="subhead">
              <div class="subttl">体重变化</div>
              <el-tag :type="deltaTagType" effect="plain">
                {{ deltaText }}
              </el-tag>
            </div>

            <div class="weight-bar">
              <div class="bar-row">
                <div class="bar-label">周初</div>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: startPct + '%' }"></div>
                </div>
                <div class="bar-val">{{ report.weightStartKg }} kg</div>
              </div>

              <div class="bar-row">
                <div class="bar-label">周末</div>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: endPct + '%' }"></div>
                </div>
                <div class="bar-val">{{ report.weightEndKg }} kg</div>
              </div>
            </div>

            <div class="insight">
              <div class="insight-title">解读建议</div>
              <div class="insight-text">
                {{ insight }}
              </div>
            </div>
          </el-card>

          <!-- 训练执行情况 -->
          <el-card shadow="never" class="subcard">
            <div class="subhead">
              <div class="subttl">训练执行情况</div>
            </div>

            <el-progress
              :percentage="workoutPct"
              :stroke-width="14"
              status="success"
            />
            <div class="muted" style="margin-top:10px;">
              说明：这里的完成度 = 训练天数 / 计划每周训练天数（来自你的计划模块）。如果你还没设置计划，默认按 3 天展示。
            </div>
          </el-card>
        </template>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { apiGetWeeklyReport, type WeeklyReportResp } from '@/api/report'
import { apiGetCurrentPlan } from '@/api/plan'
import { getWeekStartMonday } from '@/utils/week'

const loading = ref(false)
const weekStart = ref<string>(getWeekStartMonday(new Date()))
const report = ref<WeeklyReportResp | null>(null)

// 用于“训练完成度”：来自 /api/plans/current.weeklyWorkoutDays
const planWeeklyDays = ref<number>(3)

async function loadPlan() {
  try {
    const res = await apiGetCurrentPlan()
    planWeeklyDays.value = Math.max(1, Number(res.data.data.weeklyWorkoutDays || 3))
  } catch {
    planWeeklyDays.value = 3
  }
}

async function load() {
  loading.value = true
  try {
    const res = await apiGetWeeklyReport(weekStart.value)
    report.value = res.data.data
  } finally {
    loading.value = false
  }
}

const delta = computed(() => {
  if (!report.value) return 0
  return Number(report.value.weightEndKg || 0) - Number(report.value.weightStartKg || 0)
})

const deltaText = computed(() => {
  const d = delta.value
  if (d > 0) return `本周体重 +${d.toFixed(1)} kg`
  if (d < 0) return `本周体重 ${d.toFixed(1)} kg`
  return '本周体重无变化'
})

const deltaTagType = computed(() => {
  const d = delta.value
  if (d > 0) return 'warning'
  if (d < 0) return 'success'
  return 'info'
})

const insight = computed(() => {
  if (!report.value) return ''
  const d = delta.value
  const days = report.value.workoutDays

  if (d < -0.3) {
    return `体重下降比较明显（${d.toFixed(1)}kg）。如果你的目标是减脂，这是一个积极信号。建议继续关注蛋白摄入与训练强度，避免掉肌肉。训练天数：${days}天。`
  }
  if (d > 0.3) {
    return `体重上升（+${d.toFixed(1)}kg）。如果你在增肌期，这是可能出现的正常波动；如果你在减脂期，建议检查饮食热量是否超标，或是否有盐分/水分导致的短期波动。训练天数：${days}天。`
  }
  return `体重变化不大（${d.toFixed(1)}kg），属于正常波动范围。建议保持训练稳定性，同时通过饮食记录观察热量与三大营养素结构。训练天数：${days}天。`
})

// 做一个简单可视化：把两条横条映射到同一刻度
const startPct = computed(() => {
  if (!report.value) return 0
  const a = Number(report.value.weightStartKg || 0)
  const b = Number(report.value.weightEndKg || 0)
  const max = Math.max(a, b, 1)
  return Math.round((a / max) * 100)
})
const endPct = computed(() => {
  if (!report.value) return 0
  const a = Number(report.value.weightStartKg || 0)
  const b = Number(report.value.weightEndKg || 0)
  const max = Math.max(a, b, 1)
  return Math.round((b / max) * 100)
})

const workoutPct = computed(() => {
  if (!report.value) return 0
  const total = Math.max(1, planWeeklyDays.value || 3)
  const done = Math.max(0, report.value.workoutDays || 0)
  return Math.min(100, Math.round((done / total) * 100))
})

onMounted(async () => {
  await loadPlan()
  await load()
})
</script>

<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 18px; font-weight: 900; }
.actions { display: flex; gap: 10px; align-items: center; }

.kpi {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}
.kpi-item {
  background: #f3f6fb;
  border-radius: 14px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.kpi-item span { color: var(--muted); font-size: 12px; }
.kpi-item b { font-size: 18px; }

.subcard { border-radius: 18px; margin-top: 12px; }
.subhead { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.subttl { font-size: 16px; font-weight: 900; }

.weight-bar { display: flex; flex-direction: column; gap: 12px; }
.bar-row { display: grid; grid-template-columns: 60px 1fr 90px; gap: 12px; align-items: center; }
.bar-label { color: var(--muted); font-weight: 700; }
.bar-track {
  height: 12px;
  background: #eef2f7;
  border-radius: 999px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: #409eff;
  border-radius: 999px;
}
.bar-val { text-align: right; font-weight: 800; }

.insight { margin-top: 14px; padding: 12px; background: #f8fafc; border-radius: 14px; }
.insight-title { font-weight: 900; margin-bottom: 6px; }
.insight-text { color: #374151; line-height: 1.7; }

.muted { color: var(--muted); font-size: 12px; }
</style>
