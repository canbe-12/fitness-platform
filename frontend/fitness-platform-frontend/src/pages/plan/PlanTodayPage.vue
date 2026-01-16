<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">今日计划</div>
          <el-button type="primary" @click="goToEdit">编辑计划</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="4" animated />
      <div v-else-if="plan" class="grid">
        <div class="kv"><span>目标</span><b>{{ targetText }}</b></div>
        <div class="kv"><span>每周训练天数</span><b>{{ plan.weeklyWorkoutDays }} 天</b></div>
        <div class="kv"><span>每日热量目标</span><b>{{ plan.dailyKcalTarget }} kcal</b></div>
        <div class="kv"><span>蛋白目标</span><b>{{ plan.proteinTargetG }} g</b></div>
      </div>
      <el-empty v-else description="暂无计划数据">
        <el-button type="primary" @click="goToEdit">创建计划</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiGetCurrentPlan, type CurrentPlanResp } from '@/api/plan'

const router = useRouter()
const loading = ref(false)
const plan = ref<CurrentPlanResp | null>(null)

const targetText = computed(() => {
  if (!plan.value) return ''
  const map: Record<string, string> = {
    FAT_LOSS: '减脂',
    MUSCLE_GAIN: '增肌',
    MAINTAIN: '维持',
  }
  return map[plan.value.target] || plan.value.target
})

function goToEdit() {
  router.push('/plan/calendar')
}

async function load() {
  loading.value = true
  try {
    const res = await apiGetCurrentPlan()
    plan.value = res.data.data
  } catch (err: any) {
    // 如果获取失败，plan 保持为 null，显示空状态
    console.error('加载计划失败:', err)
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
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.kv { background: #f3f6fb; border-radius: 14px; padding: 14px; display: flex; flex-direction: column; gap: 6px; }
.kv span { color: var(--muted); font-size: 12px; }
.kv b { font-size: 18px; }
</style>
