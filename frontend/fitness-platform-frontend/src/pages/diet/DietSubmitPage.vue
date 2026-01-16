<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <template #header>
        <div class="h">
          <div class="title">
            饮食记录
            <el-tag v-if="currentId" class="ml8" type="success" effect="plain">已存在：id={{ currentId }}</el-tag>
            <el-tag v-else class="ml8" type="info" effect="plain">新建</el-tag>
          </div>
          <div class="actions">
            <el-button @click="reload" :loading="loading">刷新</el-button>
            <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
          </div>
        </div>
      </template>

      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="日期">
              <el-date-picker v-model="form.date" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="餐次">
              <el-select v-model="form.mealType" style="width: 100%">
                <el-option label="早餐" value="BREAKFAST" />
                <el-option label="午餐" value="LUNCH" />
                <el-option label="晚餐" value="DINNER" />
                <el-option label="加餐" value="SNACK" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="说明">
              <el-tag type="info" effect="plain">先支持手动录入；下一步接 food 搜索选择</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="toolbar">
        <el-button @click="addRow">添加一行食物</el-button>
        <el-button @click="loadTodayNutrition" plain :loading="loadingNutrition">刷新当日营养汇总</el-button>
      </div>

      <el-table :data="items" border style="width: 100%" class="tbl">
        <el-table-column label="食物名" min-width="160">
          <template #default="{ row }">
            <el-autocomplete
              v-model="row.foodName"
              :fetch-suggestions="foodSuggestions"
              placeholder="输入关键词搜索食物（如：鸡胸肉）"
              clearable
              style="width: 100%"
              @select="(it:any) => applyFoodToRow(row, it.food)"
            />

          </template>
        </el-table-column>

        <el-table-column label="品牌" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.brand" placeholder="可选" />
          </template>
        </el-table-column>

        <el-table-column label="数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.amount" :min="0" :step="1" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="单位" width="110">
          <template #default="{ row }">
            <el-input v-model="row.unit" placeholder="g/份" />
          </template>
        </el-table-column>

        <el-table-column label="kcal" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.kcal" :min="0" :step="1" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="蛋白(g)" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.proteinG" :min="0" :step="0.1" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="碳水(g)" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.carbG" :min="0" :step="0.1" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="脂肪(g)" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.fatG" :min="0" :step="0.1" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="summary">
        <el-card shadow="never" class="sum-card">
          <div class="sum-title">本次提交营养汇总（items合计）</div>
          <div class="sum-grid">
            <div class="kv"><span>kcal</span><b>{{ sum.kcal }}</b></div>
            <div class="kv"><span>蛋白(g)</span><b>{{ sum.proteinG }}</b></div>
            <div class="kv"><span>碳水(g)</span><b>{{ sum.carbG }}</b></div>
            <div class="kv"><span>脂肪(g)</span><b>{{ sum.fatG }}</b></div>
          </div>
        </el-card>

        <el-card shadow="never" class="sum-card">
          <div class="sum-title">当日营养汇总（后端统计）</div>
          <div v-if="dayNutrition" class="sum-grid">
            <div class="kv"><span>kcal</span><b>{{ dayNutrition.kcal }}</b></div>
            <div class="kv"><span>蛋白(g)</span><b>{{ dayNutrition.proteinG }}</b></div>
            <div class="kv"><span>碳水(g)</span><b>{{ dayNutrition.carbG }}</b></div>
            <div class="kv"><span>脂肪(g)</span><b>{{ dayNutrition.fatG }}</b></div>
          </div>
          <el-empty v-else description="点击“刷新当日营养汇总”加载" />
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  apiCreateDietLog,
  apiDeleteDietLog,
  apiGetDietLogsByDay,
  apiGetNutritionDay,
  apiUpdateDietLog,
  type DietLogItemReq,
  type DietLogResp,
  type MealType,
} from '@/api/diet'
import { newClientId } from '@/utils/id'
import { toYmd } from '@/utils/date'
import { apiGetFoods, type FoodResp } from '@/api/food'


function unwrapApi<T = any>(res: any): T {
  const d = res?.data
  if (d == null) return d
  if (d.data !== undefined) return d.data as T
  if (d.result !== undefined) return d.result as T
  if (d.content !== undefined) return d.content as T
  return d as T
}


const submitting = ref(false)
const loading = ref(false)
const loadingNutrition = ref(false)

const form = reactive({
  date: new Date(),
  mealType: 'LUNCH' as MealType,
})

const items = reactive<DietLogItemReq[]>([
  { foodName: '鸡胸肉', brand: '', amount: 100, unit: 'g', kcal: 165, proteinG: 31, carbG: 0, fatG: 3.6 },
])

const currentId = ref<number | null>(null) // 当前 date+meal 对应的 dietLog id（用于 PUT）
const dayNutrition = ref<any>(null)

function addRow() {
  items.push({ foodName: '', brand: '', amount: 0, unit: 'g', kcal: 0, proteinG: 0, carbG: 0, fatG: 0 })
}
function removeRow(i: number) {
  items.splice(i, 1)
}

const sum = computed(() => {
  const s = { kcal: 0, proteinG: 0, carbG: 0, fatG: 0 }
  for (const it of items) {
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
})

function normalizeFoodList(data: any): FoodResp[] {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data.records)) return data.records
  if (Array.isArray(data.list)) return data.list
  return []
}

async function queryFoods(keyword: string): Promise<FoodResp[]> {
  const res = await apiGetFoods({ keyword, page: 1, size: 20 })
  const data = unwrapApi<any>(res)
  return normalizeFoodList(data)
}

async function foodSuggestions(queryString: string, cb: (items: any[]) => void) {
  const q = (queryString || '').trim()
  if (!q) return cb([])
  try {
    const list = await queryFoods(q)
    cb(
      list.map(f => ({
        value: f.foodName,
        food: f,
      })),
    )
  } catch {
    cb([])
  }
}

function applyFoodToRow(row: any, food: FoodResp) {
  row.foodId = food.id
  row.foodName = food.foodName
  row.brand = food.brand || row.brand || ''
  row.unit = food.unit || row.unit || 'g'
  row.kcal = Number(food.kcal || 0)
  row.proteinG = Number(food.proteinG || 0)
  row.carbG = Number(food.carbG || 0)
  row.fatG = Number(food.fatG || 0)
  row.fiberG = Number(food.fiberG || 0)
  row.sodiumMg = Number(food.sodiumMg || 0)
}


function applyLogToForm(log: DietLogResp) {
  currentId.value = log.id
  items.splice(0, items.length)
  for (const it of log.items || []) {
    items.push({
      foodId: it.foodId,
      foodName: it.foodName,
      brand: it.brand || '',
      amount: Number(it.amount || 0),
      unit: it.unit || 'g',
      kcal: Number(it.kcal || 0),
      proteinG: Number(it.proteinG || 0),
      carbG: Number(it.carbG || 0),
      fatG: Number(it.fatG || 0),
      fiberG: Number(it.fiberG || 0),
      sodiumMg: Number(it.sodiumMg || 0),
    })
  }
  if (items.length === 0) addRow()
}

function resetToNew() {
  currentId.value = null
  items.splice(0, items.length)
  addRow()
}

async function loadExistingForDateMeal() {
  const date = toYmd(form.date)
  loading.value = true
  try {
    const { list } = await apiGetDietLogsByDay(date)
    const hit = list.find(x => x.mealType === form.mealType)
    if (hit) {
      applyLogToForm(hit)
    } else {
      resetToNew()
    }
  } catch (e: any) {
    resetToNew()
  } finally {
    loading.value = false
  }
}

async function loadTodayNutrition() {
  const date = toYmd(form.date)
  loadingNutrition.value = true
  try {
    const res = await apiGetNutritionDay(date)
    dayNutrition.value = res.data.data
  } finally {
    loadingNutrition.value = false
  }
}

async function reload() {
  await loadExistingForDateMeal()
  await loadTodayNutrition()
}

watch(
  () => [toYmd(form.date), form.mealType],
  async () => {
    await loadExistingForDateMeal()
    await loadTodayNutrition()
  },
  { immediate: true },
)

async function submit() {
  if (items.length === 0) {
    ElMessage.warning('请至少添加一条食物记录')
    return
  }
  if (items.some(i => !i.foodName?.trim())) {
    ElMessage.warning('存在食物名为空的行')
    return
  }

  submitting.value = true
  try {
    const payload = {
      clientRequestId: newClientId(),
      date: toYmd(form.date),
      mealType: form.mealType,
      items: items.map(i => ({
        foodId: i.foodId || 0,
        foodName: i.foodName,
        brand: i.brand || '',
        amount: Number(i.amount || 0),
        unit: i.unit || 'g',
        kcal: Number(i.kcal || 0),
        proteinG: Number(i.proteinG || 0),
        carbG: Number(i.carbG || 0),
        fatG: Number(i.fatG || 0),
        fiberG: Number(i.fiberG || 0),
        sodiumMg: Number(i.sodiumMg || 0),
      })),
    }

    if (currentId.value) {
      try {
        const res = await apiUpdateDietLog(currentId.value, payload as any)
        const saved = unwrapApi<any>(res)
        currentId.value = saved?.id ?? currentId.value
        ElMessage.success(`更新成功${saved?.id ? `：id=${saved.id}` : ''}`)
      } catch (e: any) {
        const msg = e?.response?.data?.message || e?.message || ''
        await apiDeleteDietLog(currentId.value)
        const res2 = await apiCreateDietLog(payload)
        currentId.value = res2.data.data.id
        ElMessage.success(`已改为覆盖保存：delete+create，新id=${currentId.value}${msg ? `（PUT失败：${msg}）` : ''}`)
      }
    } else {
      const res = await apiCreateDietLog(payload)
      const saved = unwrapApi<any>(res)
      currentId.value = saved?.id ?? currentId.value ?? null
      ElMessage.success(`提交成功${saved?.id ? `：id=${saved.id}` : ''}`)
    }

    await reload()
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '提交失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
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
}
.title {
  font-size: 18px;
  font-weight: 900;
  display: flex;
  align-items: center;
}
.actions {
  display: flex;
  gap: 10px;
}
.ml8 {
  margin-left: 8px;
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
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 14px;
}
.sum-card {
  border-radius: 16px;
}
.sum-title {
  font-weight: 900;
  margin-bottom: 10px;
}
.sum-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
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
  color: var(--muted);
  font-size: 12px;
}
.kv b {
  font-size: 18px;
}
</style>
