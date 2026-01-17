<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">综合健康报告</div>
          <div class="actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="shortcuts"
              style="width: 260px"
              @change="load"
            />
            <el-button :loading="loading" @click="load" plain>刷新</el-button>
            <el-button @click="printReport" plain>打印/PDF</el-button>
            <el-button @click="exportCsv" type="primary" plain>导出CSV</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />
      
      <div v-else-if="!report" class="empty-state">
        <el-empty description="暂无数据，请选择日期范围或添加记录" />
      </div>

      <div v-else class="report-content">
        <!-- 核心指标概览 -->
        <div class="summary-row">
          <div class="kpi-card">
            <div class="label">总训练次数</div>
            <div class="value">{{ report.summary.totalWorkouts }} 次</div>
          </div>
          <div class="kpi-card">
            <div class="label">总训练时长</div>
            <div class="value">{{ report.summary.totalDurationMinutes }} 分钟</div>
          </div>
          <div class="kpi-card">
            <div class="label">平均日摄入</div>
            <div class="value">{{ report.summary.avgDailyKcalIntake }} kcal</div>
          </div>
          <div class="kpi-card">
            <div class="label">训练消耗总热量</div>
            <div class="value">{{ report.summary.totalTrainingKcal }} kcal</div>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="report-tabs">
          <!-- 1. 训练记录部分 -->
          <el-tab-pane label="训练分析" name="training">
            <div class="tab-content">
              <!-- 图表区 -->
              <div class="chart-row">
                <el-card shadow="never" class="chart-card">
                  <template #header>训练容量趋势</template>
                  <div class="chart-container">
                    <v-chart :option="trainingTrendOption" autoresize />
                  </div>
                </el-card>
                <el-card shadow="never" class="chart-card">
                  <template #header>训练部位分布</template>
                  <div class="chart-container">
                    <v-chart :option="bodyPartOption" autoresize />
                  </div>
                </el-card>
              </div>

              <!-- 列表区 -->
              <el-card shadow="never" class="list-card">
                <template #header>训练历史记录</template>
                <el-table :data="report.trainingHistory" stripe style="width: 100%">
                  <el-table-column prop="date" label="日期" width="120" sortable />
                  <el-table-column label="训练部位/内容" min-width="180">
                    <template #default="{ row }">
                      <div class="tags">
                        <el-tag v-for="p in row.bodyParts" :key="p" size="small" effect="plain">{{ p }}</el-tag>
                      </div>
                      <div class="sub-text" v-if="row.exerciseNames && row.exerciseNames.length">
                        {{ row.exerciseNames.join(', ') }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="durationMinutes" label="时长(分)" width="100">
                    <template #default="{ row }">{{ row.durationMinutes }} min</template>
                  </el-table-column>
                  <el-table-column prop="caloriesBurned" label="消耗(kcal)" width="120">
                    <template #default="{ row }">{{ row.caloriesBurned }} kcal</template>
                  </el-table-column>
                  <el-table-column prop="volume" label="总容量(kg)" width="120" />
                </el-table>
              </el-card>
            </div>
          </el-tab-pane>

          <!-- 2. 饮食记录部分 -->
          <el-tab-pane label="饮食分析" name="diet">
            <div class="tab-content">
              <!-- 营养摄入概览 -->
              <div class="nutrition-summary">
                <div class="nut-item">
                  <div class="nut-label">平均蛋白质</div>
                  <div class="nut-val">{{ report.summary.avgProtein }}g</div>
                  <el-progress :percentage="50" :format="() => ''" status="success" />
                </div>
                <div class="nut-item">
                  <div class="nut-label">平均碳水</div>
                  <div class="nut-val">{{ report.summary.avgCarb }}g</div>
                  <el-progress :percentage="60" :format="() => ''" status="warning" />
                </div>
                <div class="nut-item">
                  <div class="nut-label">平均脂肪</div>
                  <div class="nut-val">{{ report.summary.avgFat }}g</div>
                  <el-progress :percentage="30" :format="() => ''" status="exception" />
                </div>
              </div>

              <div class="chart-row">
                 <el-card shadow="never" class="chart-card">
                  <template #header>热量摄入趋势</template>
                  <div class="chart-container">
                    <v-chart :option="dietTrendOption" autoresize />
                  </div>
                </el-card>
                 <el-card shadow="never" class="chart-card">
                  <template #header>三大营养素来源</template>
                  <div class="chart-container">
                    <v-chart :option="macroPieOption" autoresize />
                  </div>
                </el-card>
              </div>

              <el-card shadow="never" class="list-card">
                <template #header>每日饮食明细</template>
                <el-table :data="report.dietHistory" stripe style="width: 100%">
                  <el-table-column prop="date" label="日期" width="120" sortable />
                  <el-table-column prop="mealCount" label="餐数" width="80" />
                  <el-table-column prop="kcal" label="热量(kcal)" width="120" sortable />
                  <el-table-column prop="protein" label="蛋白质(g)" width="100" />
                  <el-table-column prop="carb" label="碳水(g)" width="100" />
                  <el-table-column prop="fat" label="脂肪(g)" width="100" />
                </el-table>
              </el-card>
            </div>
          </el-tab-pane>

          <!-- 3. 综合展示 -->
          <el-tab-pane label="综合报表" name="comprehensive">
            <div class="tab-content">
              <el-card shadow="never" class="chart-card full-width">
                <template #header>热量平衡分析 (摄入 vs 消耗)</template>
                <div class="chart-container large">
                  <v-chart :option="balanceOption" autoresize />
                </div>
                <div class="insight-box">
                  <p><strong>💡 分析建议：</strong></p>
                  <p v-if="report.summary.avgDailyKcalIntake > report.summary.totalTrainingKcal + 2000">
                    当前平均摄入热量较高，如果目标是减脂，建议控制饮食或增加有氧训练。
                  </p>
                  <p v-else-if="report.summary.avgDailyKcalIntake < 1200">
                    热量摄入偏低，请注意保证基础代谢需求，避免肌肉流失。
                  </p>
                  <p v-else>
                    热量收支相对平衡，请继续保持当前的训练与饮食节奏。
                  </p>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { apiGetComprehensiveReport, type ComprehensiveReportResp } from '@/api/report'
import { toYmd } from '@/utils/date'

// ECharts
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DataZoomComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DataZoomComponent
])

const loading = ref(false)
const activeTab = ref('training')
const report = ref<ComprehensiveReportResp | null>(null)

// 默认最近30天
const end = new Date()
const start = new Date()
start.setDate(start.getDate() - 29)
const dateRange = ref<[string, string]>([toYmd(start), toYmd(end)])

const shortcuts = [
  { text: '最近一周', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 6); return [start, end] } },
  { text: '最近一月', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 29); return [start, end] } },
  { text: '最近三月', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 90); return [start, end] } },
]

async function load() {
  if (!dateRange.value || dateRange.value.length !== 2) return
  loading.value = true
  try {
    const res = await apiGetComprehensiveReport(dateRange.value[0], dateRange.value[1])
    report.value = res.data.data
  } catch (err: any) {
    ElMessage.error(err.message || '加载报告失败')
  } finally {
    loading.value = false
  }
}

function printReport() {
  window.print()
}

function exportCsv() {
  if (!report.value) return

  const headers = ['Date', 'TotalSets', 'Duration(min)', 'TrainingKcal', 'DietKcal', 'Protein(g)', 'Carb(g)', 'Fat(g)']
  const rows: any[] = []

  // Get all unique dates
  const dates = new Set<string>()
  report.value.trainingHistory.forEach(x => dates.add(x.date))
  report.value.dietHistory.forEach(x => dates.add(x.date))
  const sortedDates = Array.from(dates).sort().reverse()

  for (const d of sortedDates) {
    const t = report.value.trainingHistory.find(x => x.date === d)
    const diet = report.value.dietHistory.find(x => x.date === d)

    rows.push([
      d,
      t ? t.setCount : 0,
      t ? t.durationMinutes : 0,
      t ? t.caloriesBurned : 0,
      diet ? diet.kcal : 0,
      diet ? diet.protein : 0,
      diet ? diet.carb : 0,
      diet ? diet.fat : 0
    ])
  }

  const csvContent = [
    headers.join(','),
    ...rows.map(r => r.join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', `report_${dateRange.value[0]}_${dateRange.value[1]}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// --- Charts Options ---

const trainingTrendOption = computed(() => {
  if (!report.value) return {}
  const data = [...report.value.trainingHistory].reverse() // chronological
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['训练时长(min)', '总容量(kg)'] },
    xAxis: { type: 'category', data: data.map(x => x.date.slice(5)) },
    yAxis: [
      { type: 'value', name: '时长', position: 'left' },
      { type: 'value', name: '容量', position: 'right' }
    ],
    series: [
      { name: '训练时长(min)', type: 'bar', data: data.map(x => x.durationMinutes), yAxisIndex: 0, itemStyle: { color: '#3b82f6' } },
      { name: '总容量(kg)', type: 'line', data: data.map(x => x.volume), yAxisIndex: 1, smooth: true, itemStyle: { color: '#f59e0b' } }
    ]
  }
})

const bodyPartOption = computed(() => {
  if (!report.value) return {}
  const map = new Map<string, number>()
  report.value.trainingHistory.forEach(d => {
    d.bodyParts.forEach(p => {
      map.set(p, (map.get(p) || 0) + 1)
    })
  })
  const data = Array.from(map.entries()).map(([name, value]) => ({ name, value }))
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: '训练部位',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        data: data.length ? data : [{ name: '无数据', value: 0 }]
      }
    ]
  }
})

const dietTrendOption = computed(() => {
  if (!report.value) return {}
  const data = [...report.value.dietHistory].reverse()
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(x => x.date.slice(5)) },
    yAxis: { type: 'value', name: 'kcal' },
    series: [
      { name: '热量摄入', type: 'line', smooth: true, data: data.map(x => x.kcal), areaStyle: { opacity: 0.1 }, itemStyle: { color: '#10b981' } }
    ]
  }
})

const macroPieOption = computed(() => {
          if (!report.value) return {}
          const s = report.value.summary
          const data = [
            { name: '蛋白质', value: s.avgProtein },
            { name: '碳水', value: s.avgCarb },
            { name: '脂肪', value: s.avgFat }
          ]
          return {
            tooltip: { trigger: 'item' },
            color: ['#3b82f6', '#10b981', '#f59e0b'],
            series: [
              {
                type: 'pie',
                radius: '60%',
                data: data.some(x => x.value > 0) ? data : [{name:'无数据', value:0}],
                label: { formatter: '{b}: {c}g ({d}%)' }
              }
            ]
          }
        })

        const mealStackOption = computed(() => {
          if (!report.value) return {}
          const data = [...report.value.dietHistory].reverse()
          const dates = data.map(x => x.date.slice(5))
          
          const getVal = (d: any, type: string) => (d.mealKcalBreakdown && d.mealKcalBreakdown[type]) || 0

          return {
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            legend: { data: ['早餐', '午餐', '晚餐', '加餐'] },
            xAxis: { type: 'category', data: dates },
            yAxis: { type: 'value', name: 'kcal' },
            series: [
              { name: '早餐', type: 'bar', stack: 'total', data: data.map(x => getVal(x, '早餐')), itemStyle: { color: '#fbbf24' } },
              { name: '午餐', type: 'bar', stack: 'total', data: data.map(x => getVal(x, '午餐')), itemStyle: { color: '#f87171' } },
              { name: '晚餐', type: 'bar', stack: 'total', data: data.map(x => getVal(x, '晚餐')), itemStyle: { color: '#60a5fa' } },
              { name: '加餐', type: 'bar', stack: 'total', data: data.map(x => getVal(x, '加餐')), itemStyle: { color: '#a78bfa' } }
            ]
          }
        })

const balanceOption = computed(() => {
  if (!report.value) return {}
  // Merge dates from both histories
  const dateSet = new Set<string>()
  report.value.trainingHistory.forEach(x => dateSet.add(x.date))
  report.value.dietHistory.forEach(x => dateSet.add(x.date))
  const dates = Array.from(dateSet).sort()
  
  const inData = dates.map(d => report.value?.dietHistory.find(x => x.date === d)?.kcal || 0)
  const outData = dates.map(d => report.value?.trainingHistory.find(x => x.date === d)?.caloriesBurned || 0)

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['摄入热量', '运动消耗'] },
    xAxis: { type: 'category', data: dates.map(d => d.slice(5)) },
    yAxis: { type: 'value' },
    dataZoom: [{ type: 'inside' }, { type: 'slider' }],
    series: [
      { name: '摄入热量', type: 'line', data: inData, smooth: true, itemStyle: { color: '#10b981' } },
      { name: '运动消耗', type: 'bar', data: outData, itemStyle: { color: '#ef4444' } }
    ]
  }
})

onMounted(() => {
  load()
})
</script>

<style scoped lang="scss">
.page {
  display: flex; flex-direction: column; gap: 16px;
  /* Print optimization */
  @media print {
    .actions, .el-tabs__nav-wrap { display: none !important; }
    .card { box-shadow: none; border: none; }
  }
}
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.title { font-size: 20px; font-weight: 800; color: var(--text-1); }
.actions { display: flex; gap: 12px; align-items: center; }

.summary-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.kpi-card {
  background: var(--surface-2);
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  .label { font-size: 13px; color: var(--text-2); }
  .value { font-size: 24px; font-weight: 700; color: var(--text-1); }
}

.report-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: var(--border); }

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-top: 16px;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
}
.chart-card {
  border-radius: 12px;
  .chart-container {
    height: 300px;
    &.large { height: 400px; }
  }
}
.list-card { border-radius: 12px; }

.tags { display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 4px; }
.sub-text { font-size: 12px; color: var(--text-2); line-height: 1.4; }
.text-muted { color: var(--text-2); font-size: 12px; }

.nutrition-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 8px;
}
.nut-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  .nut-label { font-size: 14px; color: var(--text-2); }
  .nut-val { font-size: 20px; font-weight: 600; }
}

.insight-box {
  margin-top: 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 16px;
  color: #0369a1;
  font-size: 14px;
  line-height: 1.6;
}
</style>
