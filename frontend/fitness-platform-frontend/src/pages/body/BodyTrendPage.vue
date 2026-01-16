<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">身体趋势</div>
          <div class="actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px"
            />
            <el-button :loading="loading" @click="load" plain>刷新</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="4" animated />

      <div v-else>
        <el-alert
          v-if="!trendData || trendData.length === 0"
          type="info"
          show-icon
          title="暂无数据"
          description="你可以先去设置页面记录体重，然后再回来查看趋势。"
        />

        <div v-else class="chart-container">
          <div class="chart-title">体重趋势 (kg)</div>
          <div class="chart">
            <div
              v-for="(point, index) in trendData"
              :key="point.date"
              class="point"
              :style="{ left: `${(index / (trendData.length - 1 || 1)) * 100}%` }"
            >
              <div class="point-value">{{ point.weightKg.toFixed(1) }}</div>
              <div class="point-dot"></div>
              <div class="point-date">{{ formatDate(point.date) }}</div>
            </div>
            <svg class="line" v-if="trendData.length > 1">
              <polyline
                :points="linePoints"
                fill="none"
                stroke="#409eff"
                stroke-width="2"
              />
            </svg>
          </div>
        </div>

        <el-card shadow="never" class="stats-card" style="margin-top: 16px;">
          <div class="stats">
            <div class="stat-item">
              <span>数据点数</span>
              <b>{{ trendData.length }}</b>
            </div>
            <div class="stat-item" v-if="trendData.length > 0">
              <span>起始体重</span>
              <b>{{ trendData[0].weightKg.toFixed(1) }} kg</b>
            </div>
            <div class="stat-item" v-if="trendData.length > 0">
              <span>当前体重</span>
              <b>{{ trendData[trendData.length - 1].weightKg.toFixed(1) }} kg</b>
            </div>
            <div class="stat-item" v-if="trendData.length > 1">
              <span>变化</span>
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiGetWeightTrend, type WeightTrendPoint } from '@/api/body'
import { toYmd } from '@/utils/date'

const loading = ref(false)
const dateRange = ref<[string, string]>([
  toYmd(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)), // 30天前
  toYmd(new Date()),
])
const trendData = ref<WeightTrendPoint[]>([])

const delta = computed(() => {
  if (trendData.value.length < 2) return 0
  const first = trendData.value[0].weightKg
  const last = trendData.value[trendData.value.length - 1].weightKg
  return last - first
})

const linePoints = computed(() => {
  if (trendData.value.length < 2) return ''
  const maxWeight = Math.max(...trendData.value.map(p => p.weightKg))
  const minWeight = Math.min(...trendData.value.map(p => p.weightKg))
  const range = maxWeight - minWeight || 1
  const width = 800
  const height = 300

  return trendData.value.map((point, index) => {
    const x = (index / (trendData.value.length - 1)) * width
    const y = height - ((point.weightKg - minWeight) / range) * height
    return `${x},${y}`
  }).join(' ')
})

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

async function load() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }

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

onMounted(load)
</script>

<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 18px; font-weight: 900; }
.actions { display: flex; gap: 10px; align-items: center; }

.chart-container {
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}
.chart-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #334155;
}
.chart {
  position: relative;
  height: 300px;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}
.line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.point {
  position: absolute;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.point-value {
  font-size: 12px;
  font-weight: 700;
  color: #409eff;
  background: white;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #409eff;
}
.point-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #409eff;
}
.point-date {
  font-size: 10px;
  color: #909399;
  margin-top: 4px;
}

.stats-card { border-radius: 18px; }
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.stat-item {
  background: #f3f6fb;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-item span {
  color: var(--muted);
  font-size: 12px;
}
.stat-item b {
  font-size: 18px;
  color: #334155;
}
.stat-item b.positive {
  color: #f56c6c;
}
.stat-item b.negative {
  color: #67c23a;
}
</style>

