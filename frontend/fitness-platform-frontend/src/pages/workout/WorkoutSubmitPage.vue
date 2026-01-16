<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">训练执行</div>
          <div class="right">
            <el-button @click="reload" :loading="loading">刷新</el-button>
            <el-button type="primary" :loading="submitting" @click="submit">
              提交训练日志（覆盖当天该动作）
            </el-button>
          </div>
        </div>
      </template>

      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="日期">
              <el-date-picker
                v-model="date"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="10">
            <el-form-item label="训练动作">
              <el-select
                v-model="exerciseId"
                filterable
                remote
                clearable
                reserve-keyword
                :remote-method="searchExercise"
                :loading="exerciseLoading"
                placeholder="搜索动作：深蹲/卧推/硬拉..."
                style="width: 100%"
              >
                <el-option
                  v-for="it in exerciseOptions"
                  :key="it.id"
                  :label="optionLabel(it)"
                  :value="it.id"
                />
              </el-select>
              <div class="subtip" v-if="selectedExercise">
                已选：{{ selectedExercise.name }}
                <span v-if="selectedExercise.muscleGroup">（{{ selectedExercise.muscleGroup }}）</span>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="备注（可选）">
              <el-input
                v-model="notes"
                type="textarea"
                :rows="2"
                placeholder="比如：状态一般/加了护腕/腰不舒服等"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="toolbar">
        <el-button @click="addSet">添加一组</el-button>
        <el-button @click="quick4" plain>快速填 4 组</el-button>
        <el-button @click="clearSets" plain type="warning">清空组数</el-button>
      </div>

      <el-table :data="sets" border style="width: 100%" class="tbl">
        <el-table-column label="组" width="80">
          <template #default="scope">第 {{ scope.$index + 1 }} 组</template>
        </el-table-column>

        <el-table-column label="次数 reps" width="180">
          <template #default="scope">
            <el-input-number v-model="scope.row.reps" :min="0" :step="1" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="重量 kg" width="200">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.weightKg"
              :min="0"
              :step="2.5"
              style="width: 100%"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="90" fixed="right">
          <template #default="scope">
            <el-button type="danger" link @click="removeSet(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="summary">
        <div class="kv"><span>总组数</span><b>{{ sets.length }}</b></div>
        <div class="kv"><span>总次数</span><b>{{ totalReps }}</b></div>
        <div class="kv"><span>总吨位(kg)</span><b>{{ totalVolume }}</b></div>
        <div class="kv"><span>平均重量(kg)</span><b>{{ avgWeight }}</b></div>
      </div>

      <el-divider />

      <div class="hint">
        <el-alert
          title="说明"
          type="info"
          :closable="false"
          show-icon
          description="为了实现“同一天同动作自动合并”，本页面提交时会先删除当天该动作已有日志，再按当前表格组数重新写入。这样刷新/跳转后数据也能稳定回显。"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiSearchExercises, type ExerciseResp } from '@/api/exercise'
import {
  apiCreateExerciseLog,
  apiRangeExerciseLogs,
  apiDeleteExerciseLog,
  type ExerciseLogResp,
} from '@/api/exerciseLog'
import { newClientId } from '@/utils/id'
import { toYmd } from '@/utils/date'

const date = ref(toYmd(new Date()))
const exerciseId = ref<number | null>(null)
const notes = ref('')

const sets = reactive<{ reps: number; weightKg: number }[]>([
  { reps: 5, weightKg: 60 },
  { reps: 5, weightKg: 60 },
  { reps: 5, weightKg: 60 },
])

const loading = ref(false)
const submitting = ref(false)

const exerciseLoading = ref(false)
const exerciseOptions = ref<ExerciseResp[]>([])

const selectedExercise = computed(() => {
  if (!exerciseId.value) return null
  return exerciseOptions.value.find((x) => x.id === exerciseId.value) || null
})

function optionLabel(it: ExerciseResp) {
  return it.muscleGroup ? `${it.name}（${it.muscleGroup}）` : it.name
}

function addSet() {
  sets.push({ reps: 8, weightKg: 40 })
}
function quick4() {
  sets.splice(
    0,
    sets.length,
    { reps: 8, weightKg: 40 },
    { reps: 8, weightKg: 40 },
    { reps: 8, weightKg: 40 },
    { reps: 8, weightKg: 40 }
  )
}
function clearSets() {
  sets.splice(0, sets.length)
}
function removeSet(i: number) {
  sets.splice(i, 1)
}

const totalReps = computed(() => sets.reduce((a, s) => a + Number(s.reps || 0), 0))
const totalVolume = computed(() =>
  Math.round(sets.reduce((a, s) => a + Number(s.reps || 0) * Number(s.weightKg || 0), 0))
)
const avgWeight = computed(() => {
  if (!sets.length) return 0
  const w = sets.reduce((a, s) => a + Number(s.weightKg || 0), 0) / sets.length
  return Math.round(w * 10) / 10
})

async function searchExercise(keyword: string) {
  exerciseLoading.value = true
  try {
    const res = await apiSearchExercises(keyword?.trim() || undefined, undefined, 0, 20)
    exerciseOptions.value = (res.data.data?.content || []) as ExerciseResp[]
  } finally {
    exerciseLoading.value = false
  }
}

async function loadTodayExerciseLogs() {
  if (!exerciseId.value) return

  loading.value = true
  try {
    const res = await apiRangeExerciseLogs(date.value, date.value)
    const all = (res.data.data || []) as ExerciseLogResp[]
    const list = all.filter((x) => x.exerciseId === exerciseId.value)

    // 回显：把同一天同动作的多条记录“合并成 sets”
    const mapped = list
      .slice()
      .sort((a, b) => (a.id || 0) - (b.id || 0))
      .map((x) => ({
        reps: Number(x.actualReps || 0),
        weightKg: Number(x.actualWeight || 0),
      }))

    if (mapped.length > 0) {
      sets.splice(0, sets.length, ...mapped)

      // 备注：取第一条有 notes 的
      const first = list.find((x) => x.notes && String(x.notes).trim())
      notes.value = (first?.notes as string) || ''
    }
  } finally {
    loading.value = false
  }
}

async function reload() {
  await loadTodayExerciseLogs()
}

watch([date, exerciseId], async () => {
  await loadTodayExerciseLogs()
})

async function submit() {
  if (!exerciseId.value) {
    ElMessage.warning('请先选择训练动作')
    return
  }
  if (sets.length === 0) {
    ElMessage.warning('请至少添加一组')
    return
  }

  submitting.value = true
  try {
    await ElMessageBox.confirm(
      '将覆盖当天该动作的所有训练日志（会先删除旧记录，再写入当前组数）。确认提交？',
      '确认提交',
      { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }
    )

    // 1) 查旧记录
    const rangeRes = await apiRangeExerciseLogs(date.value, date.value)
    const oldList: ExerciseLogResp[] = ((rangeRes.data.data || []) as ExerciseLogResp[]).filter(
      (x) => x.exerciseId === exerciseId.value
    )

    // 2) 删除旧记录
    if (oldList.length > 0) {
      await Promise.allSettled(oldList.map((x) => apiDeleteExerciseLog(x.id)))
    }

    // 3) 写入新记录（每组一条）
    const cid = newClientId()
    for (let i = 0; i < sets.length; i++) {
      const s = sets[i]
      await apiCreateExerciseLog({
        exerciseId: exerciseId.value,
        plannedDate: date.value,
        actualReps: Number(s.reps || 0),
        actualWeight: Number(s.weightKg || 0),
        plannedReps: 0,
        plannedWeight: 0,
        clientRequestId: `${cid}-${i + 1}`,
        notes: i === 0 ? (notes.value?.trim() || undefined) : undefined,
      })
    }

    ElMessage.success(`提交成功：已保存 ${sets.length} 组（并覆盖旧记录）`)
    await loadTodayExerciseLogs()
  } catch (e: any) {
    if (e === 'cancel' || e?.message === 'cancel') return
  } finally {
    submitting.value = false
  }
}

// 初次加载一些动作选项
searchExercise('')
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card {
  border-radius: 18px;
}
.h {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 900;
}
.right {
  display: flex;
  gap: 10px;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.tbl {
  border-radius: 12px;
  overflow: hidden;
}
.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 12px;
}
.kv {
  background: #f3f6fb;
  border-radius: 12px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.kv span {
  color: #6b7280;
  font-size: 12px;
}
.kv b {
  font-size: 18px;
}
.subtip {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}
.hint {
  margin-top: 8px;
}
</style>
