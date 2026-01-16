<template>
  <div class="page">
    <el-card class="hero" shadow="never">
      <div class="hero-inner">
        <div class="pill">上肢力量</div>
        <div class="pill">Day 1</div>

        <el-select v-model="plan" placeholder="训练项目预览" class="select">
          <el-option label="深蹲 5x5" value="squat" />
          <el-option label="卧推 5x5" value="bench" />
          <el-option label="硬拉 1x5" value="deadlift" />
        </el-select>

        <el-button type="primary" size="large" class="start" @click="go('/workout/submit')">开始训练！</el-button>
      </div>
    </el-card>

    <el-card class="card" shadow="never">
      <template #header>
        <div class="card-title">营养目标完成度</div>
      </template>
      <div class="rings">
        <StatRing title="蛋白质" :percent="dayNutrition?.proteinG ?? 0" />
        <StatRing title="碳水化合物" :percent="dayNutrition?.carbG ?? 0" />
        <StatRing title="脂肪" :percent="dayNutrition?.fatG ?? 0" />
      </div>
    </el-card>

    <div class="actions">
      <QuickActionCard title="记录饮食" desc="快速添加本次饮食" @click="go('/diet/submit')" />
      <QuickActionCard title="查看训练计划" desc="今日训练安排" @click="go('/plan/today')" />
      <QuickActionCard title="查看本周报告" desc="训练趋势与总结" @click="go('/report/weekly')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import StatRing from '@/components/dashboard/StatRing.vue'
import QuickActionCard from '@/components/dashboard/QuickActionCard.vue'
import { apiGetNutritionDay } from '@/api/diet'
import { toYmd } from '@/utils/date'

const router = useRouter()
const go = (p: string) => router.push(p)

const dayNutrition = ref<{ proteinG: number; carbG: number; fatG: number } | null>(null)

function pct(v: number, total: number) {
  if (!total) return 0
  return Math.min(100, Math.round((v / total) * 100))
}

async function load() {
  const res = await apiGetNutritionDay(toYmd(new Date()))
  const n = res.data.data
  const total = (n.proteinG || 0) + (n.carbG || 0) + (n.fatG || 0)
  dayNutrition.value = {
    proteinG: pct(n.proteinG || 0, total),
    carbG: pct(n.carbG || 0, total),
    fatG: pct(n.fatG || 0, total),
  }
}

onMounted(load)
</script>


<style scoped lang="scss">
.page { display: flex; flex-direction: column; gap: 16px; }

.hero { border-radius: 18px; }
.hero-inner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 6px;
}
.pill {
  background: #eef6ff;
  border-radius: 999px;
  padding: 10px 16px;
  font-weight: 800;
  color: #334155;
}
.select { width: 220px; margin-left: auto; }
.start { border-radius: 999px; padding: 18px 22px; font-weight: 800; }

.card { border-radius: 18px; }
.card-title { font-size: 18px; font-weight: 800; }

.rings { display: flex; gap: 18px; justify-content: space-around; padding: 8px 0; }

.actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
</style>
