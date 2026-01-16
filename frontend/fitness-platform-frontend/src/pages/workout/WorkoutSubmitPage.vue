<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">训练执行</div>
          <el-button type="primary" :loading="submitting" @click="submit">提交训练日志</el-button>
        </div>
      </template>

      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="日期">
              <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>

          <el-col :span="9">
            <el-form-item label="动作名称">
              <el-input v-model="exerciseName" placeholder="如：深蹲 / 卧推 / 硬拉" />
            </el-form-item>
          </el-col>

          <el-col :span="9">
            <el-form-item label="肌群（可选）">
              <el-input v-model="muscleGroup" placeholder="如：腿 / 胸 / 背" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注（可选）">
              <el-input v-model="note" type="textarea" :rows="2" placeholder="比如：状态一般/加了护腕/腰不舒服等" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="toolbar">
        <el-button @click="addSet">添加一组</el-button>
        <el-button @click="addCommonSets" plain>快速填 4 组</el-button>
      </div>

      <el-table :data="sets" border style="width:100%;" class="tbl">
        <el-table-column label="组" width="80">
          <template #default="{ $index }">第 {{ $index + 1 }} 组</template>
        </el-table-column>

        <el-table-column label="次数 reps" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.reps" :min="0" :step="1" style="width:100%;" />
          </template>
        </el-table-column>

        <el-table-column label="重量 kg" width="200">
          <template #default="{ row }">
            <el-input-number v-model="row.weightKg" :min="0" :step="2.5" style="width:100%;" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeSet($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="summary">
        <div class="kv"><span>总组数</span><b>{{ sets.length }}</b></div>
        <div class="kv"><span>总次数</span><b>{{ totalReps }}</b></div>
        <div class="kv"><span>总吨位(kg)</span><b>{{ totalVolume }}</b></div>
        <div class="kv"><span>平均重量(kg)</span><b>{{ avgWeight }}</b></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiCreateExerciseLog } from '@/api/exerciseLog'
import { apiSearchExercises } from '@/api/exercise'
import { newClientId } from '@/utils/id'
import { toYmd } from '@/utils/date'

const submitting = ref(false)
const date = ref(toYmd(new Date()))
const exerciseName = ref('深蹲')
const muscleGroup = ref('腿')
const note = ref('')

const sets = reactive<{ reps: number; weightKg: number }[]>([
  { reps: 5, weightKg: 60 },
  { reps: 5, weightKg: 60 },
  { reps: 5, weightKg: 60 },
])

function addSet() {
  sets.push({ reps: 8, weightKg: 40 })
}
function addCommonSets() {
  sets.splice(0, sets.length, { reps: 8, weightKg: 40 }, { reps: 8, weightKg: 40 }, { reps: 8, weightKg: 40 }, { reps: 8, weightKg: 40 })
}
function removeSet(i: number) {
  sets.splice(i, 1)
}

const totalReps = computed(() => sets.reduce((a, s) => a + Number(s.reps || 0), 0))
const totalVolume = computed(() => Math.round(sets.reduce((a, s) => a + Number(s.reps || 0) * Number(s.weightKg || 0), 0)))
const avgWeight = computed(() => {
  if (!sets.length) return 0
  const w = sets.reduce((a, s) => a + Number(s.weightKg || 0), 0) / sets.length
  return Math.round(w * 10) / 10
})

async function submit() {
  if (!exerciseName.value.trim()) {
    ElMessage.warning('请输入动作名称')
    return
  }
  if (sets.length === 0) {
    ElMessage.warning('请至少添加一组')
    return
  }

  submitting.value = true
  try {
    // 先查找 exercise
    const searchRes = await apiSearchExercises(exerciseName.value.trim(), muscleGroup.value?.trim() || undefined, 0, 1)
    let exerciseId: number | null = null
    
    if (searchRes.data.data.content && searchRes.data.data.content.length > 0) {
      // 找到匹配的 exercise，使用第一个
      exerciseId = searchRes.data.data.content[0].id
    } else {
      // 没找到，提示用户
      ElMessage.error(`未找到动作"${exerciseName.value}"，请先在系统中添加该动作`)
      return
    }

    // 为每组创建一个提交记录
    const results = []
    for (let i = 0; i < sets.length; i++) {
      const set = sets[i]
      const payload = {
        clientRequestId: `${newClientId()}-${i}`,
        exerciseId: exerciseId,
        plannedDate: date.value,
        actualWeight: Number(set.weightKg || 0),
        actualReps: Number(set.reps || 0),
        notes: i === 0 ? (note.value?.trim() || undefined) : undefined, // 只在第一组添加备注
      }
      const res = await apiCreateExerciseLog(payload as any)
      results.push(res.data.data.id)
    }
    
    ElMessage.success(`提交成功：共 ${results.length} 组，ID=${results.join(', ')}`)
    
    // 清空表单（可选）
    // sets.splice(0, sets.length)
    // note.value = ''
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '提交失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 18px; font-weight: 900; }
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; }
.tbl { border-radius: 12px; overflow: hidden; }

.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 12px;
}
.kv { background: #f3f6fb; border-radius: 12px; padding: 10px; display: flex; flex-direction: column; gap: 6px; }
.kv span { color: var(--muted); font-size: 12px; }
.kv b { font-size: 18px; }
</style>
