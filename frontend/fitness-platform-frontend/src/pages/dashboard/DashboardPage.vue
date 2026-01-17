<template>
  <!-- 首页：现代化视觉 + 清晰信息层次 + 响应式布局 -->
  <div class="container page">
    <!-- 英雄区：品牌标识 + 关键行动 -->
    <section class="hero card-base">
      <div class="hero-bg" aria-hidden="true"></div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="gradient-title">今天，继续变强</h1>
          <p class="sub">个性化训练与营养管理，助你达成目标</p>
          <div class="hero-actions">
            <AppButton size="lg" variant="primary" @click="go('/workout/submit')">开始训练</AppButton>
            <AppButton size="lg" variant="outline" @click="go('/plan/today')">查看今日计划</AppButton>
          </div>
        </div>
        <div class="hero-side">
          <!-- 预览选择 -->
          <el-select v-model="plan" placeholder="预览训练项目" size="large" class="select">
            <el-option label="深蹲 5x5" value="squat" />
            <el-option label="卧推 5x5" value="bench" />
            <el-option label="硬拉 1x5" value="deadlift" />
          </el-select>
          <div class="hero-pills">
            <span class="pill">上肢力量</span>
            <span class="pill">第 1 天</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计区：营养完成度环形图 -->
    <section class="stats card-base">
      <header class="stats-head">
        <h2>营养目标完成度</h2>
        <small class="muted">按今日摄入计算</small>
      </header>
      <div class="rings">
        <StatRing title="蛋白质" :percent="dayNutrition?.proteinG ?? 0" />
        <StatRing title="碳水化合物" :percent="dayNutrition?.carbG ?? 0" />
        <StatRing title="脂肪" :percent="dayNutrition?.fatG ?? 0" />
      </div>
    </section>

    <!-- 快捷行动：图标 + 文案 -->
    <section class="actions">
      <QuickActionCard
        title="记录饮食"
        desc="快速添加本次饮食"
        :icon="icons.diet"
        @click="go('/diet/submit')"
      />
      <QuickActionCard
        title="记录体重"
        desc="更新今日体重数据"
        :icon="icons.report"
        @click="openWeightDialog"
      />
      <QuickActionCard
        title="查看训练计划"
        desc="今日训练安排"
        :icon="icons.workout"
        @click="go('/plan')"
      />
      <QuickActionCard
        title="查看本周报告"
        desc="训练趋势与总结"
        :icon="icons.report"
        @click="go('/report/weekly')"
      />
    </section>

    <!-- 体重记录弹窗 -->
    <el-dialog v-model="weightDialogVisible" title="记录体重" width="90%" style="max-width:400px">
      <el-form :model="weightForm" label-position="top">
        <el-form-item label="日期">
          <el-date-picker 
            v-model="weightForm.date" 
            type="date" 
            value-format="YYYY-MM-DD" 
            style="width: 100%" 
            :clearable="false"
          />
        </el-form-item>
        <el-form-item label="体重 (kg)">
          <el-input-number 
            v-model="weightForm.weightKg" 
            :min="0" 
            :max="300" 
            :precision="1" 
            :step="0.1" 
            style="width: 100%" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="weightDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleWeightSubmit" :loading="weightSubmitting">
            保存记录
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatRing from '@/components/dashboard/StatRing.vue'
import QuickActionCard from '@/components/dashboard/QuickActionCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import { apiGetNutritionDay } from '@/api/diet'
import { apiUpsertWeight } from '@/api/body'
import { toYmd } from '@/utils/date'
import dietIcon from '@/assets/icons/diet.svg'
import workoutIcon from '@/assets/icons/workout.svg'
import reportIcon from '@/assets/icons/report.svg'

const router = useRouter()
const go = (p: string) => router.push(p)

const dayNutrition = ref<{ proteinG: number; carbG: number; fatG: number } | null>(null)
const plan = ref<string>('')
const icons = { diet: dietIcon, workout: workoutIcon, report: reportIcon }

// 体重记录相关
const weightDialogVisible = ref(false)
const weightSubmitting = ref(false)
const weightForm = ref({
  date: toYmd(new Date()),
  weightKg: 60.0
})

function openWeightDialog() {
  weightForm.value.date = toYmd(new Date())
  // 这里可以优化为获取用户当前体重，暂时默认为上次输入或60
  weightDialogVisible.value = true
}

async function handleWeightSubmit() {
  if (!weightForm.value.weightKg) {
    ElMessage.warning('请输入有效体重')
    return
  }
  
  weightSubmitting.value = true
  try {
    await apiUpsertWeight(weightForm.value.date, { weightKg: weightForm.value.weightKg })
    ElMessage.success('体重记录成功')
    weightDialogVisible.value = false
  } catch (error) {
    console.error(error)
  } finally {
    weightSubmitting.value = false
  }
}

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


<style scoped>
.page { display: flex; flex-direction: column; gap: 18px; }

/* 英雄区 */
.hero { position: relative; overflow: hidden; padding: 24px; }
.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(600px 160px at 10% 30%, rgba(34,211,238,.12) 0%, transparent 60%),
    radial-gradient(600px 160px at 90% 20%, rgba(59,130,246,.12) 0%, transparent 60%),
    linear-gradient(180deg, rgba(255,255,255,0) 0%, rgba(255,255,255,.4) 100%);
  filter: blur(8px);
}
.hero-content { position: relative; display: grid; grid-template-columns: 1.6fr 1fr; gap: 20px; }
.hero-text .sub { margin-top: 8px; color: var(--muted); }
.hero-actions { margin-top: 16px; display: flex; gap: 10px; flex-wrap: wrap; }
.hero-side { display: flex; flex-direction: column; gap: 10px; align-items: flex-end; }
.hero-pills { display: flex; gap: 8px; flex-wrap: wrap; }
.pill {
  background: rgba(59,130,246,.1);
  border: 1px solid rgba(59,130,246,.2);
  color: var(--primary-700);
  padding: 8px 12px;
  border-radius: 999px;
  font-weight: 700;
}
.select { width: 240px; }

/* 统计区 */
.stats { padding: 16px 18px; }
.stats-head { display: flex; align-items: baseline; gap: 10px; }
.stats-head h2 { font-size: 18px; font-weight: 800; }
.muted { color: var(--muted); }
.rings { display: grid; grid-template-columns: repeat(3, minmax(160px, 1fr)); gap: 18px; padding: 8px 0; }

/* 快捷区 */
.actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

/* 响应式 */
@media (max-width: 960px) {
  .hero-content { grid-template-columns: 1fr; }
  .hero-side { align-items: flex-start; }
  .select { width: 100%; }
}
@media (max-width: 720px) {
  .actions { grid-template-columns: 1fr; }
  .rings { grid-template-columns: 1fr; }
}
</style>
