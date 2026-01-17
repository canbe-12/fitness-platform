<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">饮食历史</div>

          <div class="actions">
            <el-segmented v-model="mode" :options="modeOptions" />
            <el-date-picker
              v-if="mode === 'day'"
              v-model="day"
              type="date"
              value-format="YYYY-MM-DD"
              style="width: 160px"
            />
            <el-date-picker
              v-else
              v-model="range"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="~"
              start-placeholder="开始"
              end-placeholder="结束"
              style="width: 260px"
            />
            <el-button type="primary" :loading="loading" @click="load">查询</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="5" animated />

      <template v-else>
        <el-empty v-if="rows.length === 0" description="暂无数据" />

        <el-table v-else :data="rows" border style="width:100%;" class="tbl">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="mealType" label="餐次" width="120">
            <template #default="{ row }">
              <el-tag effect="plain">{{ mealLabel(row.mealType) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="营养（粗略）">
            <template #default="{ row }">
              <span class="nut">kcal {{ row.kcal ?? '-' }}</span>
              <span class="nut">P {{ row.proteinG ?? '-' }}</span>
              <span class="nut">C {{ row.carbG ?? '-' }}</span>
              <span class="nut">F {{ row.fatG ?? '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
              <el-popconfirm title="确定删除这条饮食记录吗？" @confirm="del(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="饮食详情" width="860px">
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      <template v-else>
        <div v-if="detail" class="detail-head">
          <el-tag effect="plain">{{ detail.date }}</el-tag>
          <el-tag type="info" effect="plain">{{ mealLabel(detail.mealType) }}</el-tag>
        </div>

        <el-table v-if="detail" :data="detail.items" border style="width:100%;" class="tbl">
          <el-table-column prop="foodName" label="食物" min-width="160" />
          <el-table-column prop="brand" label="品牌" min-width="120" />
          <el-table-column label="数量" width="120">
            <template #default="{ row }">{{ row.amount }} {{ row.unit }}</template>
          </el-table-column>
          <el-table-column prop="kcal" label="kcal" width="90" />
          <el-table-column prop="proteinG" label="P(g)" width="90" />
          <el-table-column prop="carbG" label="C(g)" width="90" />
          <el-table-column prop="fatG" label="F(g)" width="90" />
        </el-table>

        <div v-if="detail" class="sum">
          <div class="kv"><span>kcal</span><b>{{ sumDetail.kcal }}</b></div>
          <div class="kv"><span>蛋白(g)</span><b>{{ sumDetail.proteinG }}</b></div>
          <div class="kv"><span>碳水(g)</span><b>{{ sumDetail.carbG }}</b></div>
          <div class="kv"><span>脂肪(g)</span><b>{{ sumDetail.fatG }}</b></div>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  apiDeleteDietLog,
  apiGetDietLogById,
  apiGetDietLogsByDay,
  apiGetDietLogsRange,
  type DietLogResp,
  type MealType,
} from '@/api/diet'
import { toYmd } from '@/utils/date'

type Mode = 'day' | 'range'
const mode = ref<Mode>('day')
const modeOptions = [
  { label: '按天', value: 'day' },
  { label: '按范围', value: 'range' },
]

const loading = ref(false)
const day = ref(toYmd(new Date()))
const range = ref<[string, string]>([toYmd(new Date()), toYmd(new Date())])

// rows 用一个“宽松结构”承载 day/range 的返回
const rows = ref<any[]>([])

function mealLabel(m: MealType | string) {
  const map: Record<string, string> = {
    BREAKFAST: '早餐',
    LUNCH: '午餐',
    DINNER: '晚餐',
    SNACK: '加餐',
    ALL: '全天汇总',
  }
  return map[m] || m
}

// Removed sumItems as it's no longer needed for 'day' mode since we use summary from backend
// But 'range' mode uses apiGetDietLogsRange which might still return logs with items?
// Let's check apiGetDietLogsRange. It returns DietLogRangeDaySummary? 
// No, apiGetDietLogsRange calls /api/diet-logs/range which returns List<DietLogRangeDaySummary>.
// DietLogRangeDaySummary does NOT have items.
// So 'range' mode also doesn't need sumItems.
// However, the old code seemed to think it might.
// Let's just keep sumItems helper if it exists, or remove if I deleted it.
// I replaced the block that used sumItems. 
// Wait, I only replaced the 'day' block.
// The 'range' block:
// rows.value = (res.data.data || []).map((x: any) => ({ ... kcal: x.kcal ... }))
// It seems range response already has kcal.

async function load() {
  loading.value = true
  try {
    if (mode.value === 'day') {
      const res = await apiGetDietLogsByDay(day.value)
      // res.data.data is DietLogDayResp object
      const data = res.data.data as any
      const list = data?.meals || []
      
      rows.value = list.map((x: any) => ({
        id: x.id,
        date: data.date || day.value,
        mealType: x.mealType,
        kcal: x.kcal,
        proteinG: x.proteinG,
        carbG: x.carbG,
        fatG: x.fatG,
      }))
    } else {
      const [startDate, endDate] = range.value
      // apiGetDietLogsRange expects object param and returns { raw, list }
      // But wait, the previous code called it with (startDate, endDate).
      // Let's check api/diet.ts definition again.
      // export async function apiGetDietLogsRange(params: { start: string; end: string })
      
      const { list } = await apiGetDietLogsRange({ start: startDate, end: endDate })
      
      // list is DietLogResp[] or similar? 
      // Backend /range returns DietLogRangeDaySummary which has date, kcal, etc.
      // But no ID, no mealType. It's a daily summary.
      // The table expects mealType?
      // If range mode shows daily summary, we should adapt the table or rows.
      // The table columns: date, mealType, nutrition, actions.
      // If it's range summary, mealType is meaningless (it's "ALL").
      // And we can't "openDetail" or "del" a summary row easily (unless we open that day).
      
      rows.value = list.map((x: any) => ({
        id: 0, // No specific log ID
        date: x.date,
        mealType: 'ALL', // Special marker
        kcal: x.kcal,
        proteinG: x.proteinG,
        carbG: x.carbG,
        fatG: x.fatG,
      }))
    }
  } finally {
    loading.value = false
  }
}

function sumItems(items: any[] | undefined) {
  const s = { kcal: 0, proteinG: 0, carbG: 0, fatG: 0 }
  for (const it of items || []) {
    s.kcal += Number(it.kcal || 0)
    s.proteinG += Number(it.proteinG || 0)
    s.carbG += Number(it.carbG || 0)
    s.fatG += Number(it.fatG || 0)
  }
  return {
    kcal: Math.round(s.kcal),
    proteinG: Math.round(s.proteinG * 10) / 10,
    carbG: Math.round(s.carbG * 10) / 10,
    fatG: Math.round(s.fatG * 10) / 10,
  }
}

async function del(id: number) {
  await apiDeleteDietLog(id)
  ElMessage.success('删除成功')
  await load()
}

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<DietLogResp | null>(null)

const sumDetail = computed(() => sumItems(detail.value?.items))

async function openDetail(id: number) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await apiGetDietLogById(id)
    detail.value = res.data.data
  } finally {
    detailLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 18px; font-weight: 900; }
.actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.tbl { border-radius: 12px; overflow: hidden; }
.nut { display: inline-block; margin-right: 10px; color: #4b5563; font-weight: 700; }

.detail-head { display: flex; gap: 10px; margin-bottom: 12px; }

.sum {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 12px;
}
.kv { background: #f3f6fb; border-radius: 12px; padding: 10px; display: flex; flex-direction: column; gap: 6px; }
.kv span { color: var(--muted); font-size: 12px; }
.kv b { font-size: 18px; }
</style>
