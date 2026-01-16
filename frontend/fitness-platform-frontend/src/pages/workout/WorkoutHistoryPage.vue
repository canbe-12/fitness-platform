<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">训练历史</div>

          <div class="actions">
            <el-date-picker
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

      <el-skeleton v-if="loading" :rows="6" animated />
      <template v-else>
        <el-empty v-if="rows.length === 0" description="暂无数据" />

        <el-table v-else :data="rows" border style="width:100%;" class="tbl">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="exerciseName" label="动作" min-width="160" />
          <el-table-column prop="muscleGroup" label="肌群" width="120" />
          <el-table-column label="组数" width="90">
            <template #default="{ row }">{{ row.sets?.length ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="总吨位(kg)">
            <template #default="{ row }">{{ calcVolume(row.sets) }}</template>
          </el-table-column>

          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-popconfirm title="确定删除这条训练记录吗？" @confirm="del(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiDeleteExerciseLog, apiRangeExerciseLogs, type ExerciseLogResp } from '@/api/exerciseLog'
import { toYmd } from '@/utils/date'

const loading = ref(false)
const today = toYmd(new Date())
const range = ref<[string, string]>([today, today])
const rows = ref<ExerciseLogResp[]>([])

function calcVolume(sets: any[] | undefined) {
  let v = 0
  for (const s of sets || []) v += Number(s.reps || 0) * Number(s.weightKg || 0)
  return Math.round(v)
}

async function load() {
  loading.value = true
  try {
    const [startDate, endDate] = range.value
    const res = await apiRangeExerciseLogs(startDate, endDate)
    rows.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

async function del(id: number) {
  await apiDeleteExerciseLog(id)
  ElMessage.success('删除成功')
  await load()
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
</style>
