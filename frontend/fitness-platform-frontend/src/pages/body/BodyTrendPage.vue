<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">身体趋势</div>
          <div class="actions">
            <el-radio-group v-model="activeRange" size="small" @change="handleRangeChange">
              <el-radio-button label="week">近一周</el-radio-button>
              <el-radio-button label="month">近一月</el-radio-button>
              <el-radio-button label="quarter">近三月</el-radio-button>
              <el-radio-button label="custom">自定义</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-if="activeRange === 'custom'"
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
              size="small"
              @change="load"
            />
            <el-button :loading="loading" @click="load" plain size="small" icon="Refresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else>
        <el-alert
          v-if="!trendData || trendData.length === 0"
          type="info"
          show-icon
          title="暂无数据"
          description="你可以先去设置页面记录体重，然后再回来查看趋势。"
        />

        <div v-else class="chart-container">
          <div class="chart-header">
            <div class="chart-title">体重变化趋势 (kg)</div>
            <div class="chart-legend">
              <span class="dot real"></span> 实际体重
              <span class="dot trend"></span> 趋势线
            </div>
          </div>
          
          <div class="chart-wrapper">
            <v-chart :option="chartOption" autoresize class="echarts-chart" />
          </div>
        </div>

        <el-card shadow="never" class="stats-card" style="margin-top: 16px;">
          <div class="stats">
            <div class="stat-item">
              <span>数据点数</span>
              <b>{{ trendData.length }}</b>
            </div>
            <div class="stat-item" v-if="trendData.length > 0">
              <span>当前体重</span>
              <b>{{ currentWeight.toFixed(1) }} kg</b>
            </div>
             <div class="stat-item" v-if="trendData.length > 0">
              <span>平均体重</span>
              <b>{{ avgWeight.toFixed(1) }} kg</b>
            </div>
             <div class="stat-item" v-if="trendData.length > 0">
              <span>最低 / 最高</span>
              <b>{{ minWeight.toFixed(1) }} / {{ maxWeight.toFixed(1) }}</b>
            </div>
            <div class="stat-item" v-if="trendData.length > 1">
              <span>阶段变化</span>
              <b :class="{ positive: delta > 0, negative: delta < 0 }">
                {{ delta > 0 ? '+' : '' }}{{ delta.toFixed(1) }} kg
              </b>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { apiGetWeightTrend, type WeightTrendPoint } from '@/api/body'
import { apiMe } from '@/api/user'
import { toYmd } from '@/utils/date'

// ECharts
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DataZoomComponent,
  MarkLineComponent,
  MarkPointComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DataZoomComponent,
  MarkLineComponent,
  MarkPointComponent
])

const loading = ref(false)
const activeRange = ref('month')
const dateRange = ref<[string, string]>([
  toYmd(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)),
  toYmd(new Date()),
])
const trendData = ref<WeightTrendPoint[]>([])
const userHeightCm = ref(0)

// Stats
const currentWeight = computed(() => trendData.value.length ? trendData.value[trendData.value.length - 1].weightKg : 0)
const avgWeight = computed(() => {
  if (!trendData.value.length) return 0
  const sum = trendData.value.reduce((acc, cur) => acc + cur.weightKg, 0)
  return sum / trendData.value.length
})
const minWeight = computed(() => trendData.value.length ? Math.min(...trendData.value.map(p => p.weightKg)) : 0)
const maxWeight = computed(() => trendData.value.length ? Math.max(...trendData.value.map(p => p.weightKg)) : 0)

const delta = computed(() => {
  if (trendData.value.length < 2) return 0
  const first = trendData.value[0].weightKg
  const last = trendData.value[trendData.value.length - 1].weightKg
  return last - first
})

const standardWeight = computed(() => {
  if (!userHeightCm.value) return 0
  const h = userHeightCm.value / 100
  return 22 * h * h
})

// Chart Option
const chartOption = computed(() => {
  if (!trendData.value || !trendData.value.length) return {}
  
  const dates = trendData.value.map(p => p.date.slice(5)) // MM-DD
  const values = trendData.value.map((p, index) => {
    // Detect abnormal fluctuation (> 1.5kg change from previous)
    let itemStyle = { color: '#3b82f6' }
    let symbolSize = 8
    
    if (index > 0) {
      const prev = trendData.value[index - 1].weightKg
      const diff = Math.abs(p.weightKg - prev)
      if (diff > 1.5) {
        itemStyle = { color: '#f59e0b' } // Orange for abnormal
        symbolSize = 10
      }
    }
    
    return {
      value: p.weightKg,
      itemStyle,
      symbolSize
    }
  })
  
  // Calculate simple linear regression for trend line
  const rawValues = trendData.value.map(p => p.weightKg)
  const trendLineData = []
  if (rawValues.length > 1) {
    const n = rawValues.length
    let sumX = 0, sumY = 0, sumXY = 0, sumXX = 0
    for (let i = 0; i < n; i++) {
      sumX += i
      sumY += rawValues[i]
      sumXY += i * rawValues[i]
      sumXX += i * i
    }
    const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
    const intercept = (sumY - slope * sumX) / n
    for (let i = 0; i < n; i++) {
      trendLineData.push(parseFloat((slope * i + intercept).toFixed(2)))
    }
  }

  const markLineData: any[] = [{ type: 'average', name: '平均值' }]
  if (standardWeight.value > 0) {
    markLineData.push({
      yAxis: standardWeight.value,
      name: '标准体重',
      lineStyle: { color: '#10b981', width: 2 },
      label: { formatter: '标准 {c}kg', position: 'start' }
    })
  }

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      textStyle: { color: '#1e293b' },
      formatter: (params: any) => {
        const p = params[0]
        const date = trendData.value[p.dataIndex].date
        let html = `<div style="font-weight:bold;margin-bottom:4px">${date}</div>`
        params.forEach((item: any) => {
          // If it's the trend line, seriesName is '趋势线'
          // If it's the actual weight, seriesName is '实际体重'
          if (item.seriesName === '趋势线' || item.seriesName === '实际体重') {
            html += `<div style="display:flex;justify-content:space-between;gap:12px">
              <span style="color:${item.color}">● ${item.seriesName}</span>
              <span style="font-weight:bold">${item.value} kg</span>
            </div>`
          }
        })
        return html
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        type: 'slider',
        bottom: 0,
        height: 20,
        handleSize: '100%'
      }
    ],
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#94a3b8' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'value',
      scale: true, // Auto scale min/max
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } },
      axisLabel: { formatter: '{value} kg', color: '#64748b' }
    },
    series: [
      {
        name: '实际体重',
        type: 'line',
        data: values,
        smooth: true,
        symbolSize: 8,
        itemStyle: { color: '#3b82f6' },
        lineStyle: { width: 3, shadowColor: 'rgba(59, 130, 246, 0.3)', shadowBlur: 10 },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0)' }
            ]
          }
        },
        markPoint: {
          data: [
            { type: 'max', name: '最大值', itemStyle: { color: '#ef4444' } },
            { type: 'min', name: '最小值', itemStyle: { color: '#10b981' } }
          ],
          symbolSize: 40,
          label: { fontSize: 10 }
        },
        markLine: {
          data: markLineData,
          lineStyle: { type: 'dotted', color: '#64748b' },
          label: { position: 'end', formatter: '平均 {c}' }
        }
      },
      {
        name: '趋势线',
        type: 'line',
        data: trendLineData,
        smooth: false,
        symbol: 'none',
        lineStyle: { type: 'dashed', width: 2, color: '#f59e0b' },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }
})

function handleRangeChange(val: string) {
  const end = new Date()
  const start = new Date()
  
  if (val === 'week') {
    start.setDate(end.getDate() - 7)
  } else if (val === 'month') {
    start.setDate(end.getDate() - 30)
  } else if (val === 'quarter') {
    start.setDate(end.getDate() - 90)
  } else {
    // custom, do nothing, keep existing or default
    return
  }
  
  dateRange.value = [toYmd(start), toYmd(end)]
  load()
}

async function load() {
  if (!dateRange.value || dateRange.value.length !== 2) return

  loading.value = true
  try {
    const res = await apiGetWeightTrend(dateRange.value[0], dateRange.value[1])
    trendData.value = res.data.data
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '加载失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  apiMe().then(res => {
    userHeightCm.value = res.data.data.heightCm || 0
  })
})
</script>

<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.title { font-size: 18px; font-weight: 900; color: var(--text-1); }
.actions { display: flex; gap: 12px; align-items: center; }

.chart-container {
  padding: 24px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid var(--border);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.chart-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}
.chart-legend {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-2);
  align-items: center;
  
  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    &.real { background: #3b82f6; }
    &.trend { background: #f59e0b; }
  }
}

.chart-wrapper {
  height: 400px;
  width: 100%;
}
.echarts-chart {
  width: 100%;
  height: 100%;
}

.stats-card { border-radius: 18px; }
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}
.stat-item {
  background: var(--surface-2);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-item span {
  color: var(--text-2);
  font-size: 13px;
}
.stat-item b {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1);
}
.stat-item b.positive { color: #f56c6c; }
.stat-item b.negative { color: #10b981; } // Green for weight loss is usually good? Or stick to standard colors? Standard: Red = Rise, Green = Fall.
</style>

