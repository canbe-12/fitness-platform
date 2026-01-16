<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">日历计划</div>
          <el-button type="primary" :loading="saving" @click="save">保存计划</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="4" animated />

      <el-form v-else label-position="top" :model="form">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="目标">
              <el-select v-model="form.target" style="width:100%;">
                <el-option label="减脂" value="FAT_LOSS" />
                <el-option label="增肌" value="MUSCLE_GAIN" />
                <el-option label="维持" value="MAINTAIN" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="每周训练天数">
              <el-input-number v-model="form.weeklyWorkoutDays" :min="0" :max="7" style="width:100%;" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="每日热量目标 (kcal)">
              <el-input-number v-model="form.dailyKcalTarget" :min="0" :step="50" style="width:100%;" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="蛋白目标 (g)">
              <el-input-number v-model="form.proteinTargetG" :min="0" :step="5" :precision="1" style="width:100%;" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-alert
        v-if="!loading && plan"
        type="info"
        show-icon
        :closable="false"
        style="margin-top: 16px;"
      >
        <template #title>
          <div>计划信息</div>
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            创建时间：{{ plan.createdAt }}<br />
            更新时间：{{ plan.updatedAt }}
          </div>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiGetCurrentPlan, apiUpsertPlan, type CurrentPlanResp, type PlanUpsertReq } from '@/api/plan'

const loading = ref(false)
const saving = ref(false)
const plan = ref<CurrentPlanResp | null>(null)

const form = reactive<PlanUpsertReq>({
  target: 'MAINTAIN',
  weeklyWorkoutDays: 3,
  dailyKcalTarget: 1800,
  proteinTargetG: 120,
})

async function load() {
  loading.value = true
  try {
    const res = await apiGetCurrentPlan()
    plan.value = res.data.data
    form.target = res.data.data.target
    form.weeklyWorkoutDays = res.data.data.weeklyWorkoutDays
    form.dailyKcalTarget = res.data.data.dailyKcalTarget
    form.proteinTargetG = Number(res.data.data.proteinTargetG)
  } catch (err: any) {
    ElMessage.error('加载计划失败：' + (err?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const res = await apiUpsertPlan(form)
    plan.value = res.data.data
    ElMessage.success('保存成功')
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '保存失败'
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 18px; font-weight: 900; }
</style>

