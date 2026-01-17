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
            <div class="kv">
              <span>kcal</span>
              <b>{{ dayNutrition.kcal }}</b>
              <small v-if="nutritionTarget?.kcal" class="target">/ {{ nutritionTarget.kcal }}</small>
            </div>
            <div class="kv">
              <span>蛋白(g)</span>
              <b>{{ dayNutrition.proteinG }}</b>
              <small v-if="nutritionTarget?.proteinG" class="target">/ {{ nutritionTarget.proteinG }}</small>
            </div>
            <div class="kv">
              <span>碳水(g)</span>
              <b>{{ dayNutrition.carbG }}</b>
              <small v-if="nutritionTarget?.carbG" class="target">/ {{ nutritionTarget.carbG }}</small>
            </div>
            <div class="kv">
              <span>脂肪(g)</span>
              <b>{{ dayNutrition.fatG }}</b>
              <small v-if="nutritionTarget?.fatG" class="target">/ {{ nutritionTarget.fatG }}</small>
            </div>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </div>
    </el-card>

    <!-- Food Selection Dialog -->
    <el-dialog v-model="foodDialogVisible" title="选择食物" width="600px" append-to-body>
      <div class="food-search">
        <el-input
          v-model="foodKeyword"
          placeholder="搜索食物名称..."
          clearable
          @keyup.enter="searchFood"
          style="width: 300px"
        >
          <template #append>
            <el-button @click="searchFood" :loading="foodLoading">搜索</el-button>
          </template>
        </el-input>
      </div>
      
      <el-table :data="foodList" v-loading="foodLoading" style="width: 100%; margin-top: 12px;" max-height="400">
        <el-table-column prop="foodName" label="名称" />
        <el-table-column prop="unit" label="单位" width="100" />
        <el-table-column prop="kcal" label="热量" width="80" />
        <el-table-column prop="proteinG" label="蛋白" width="80" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="selectFood(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
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
  type DietNutritionSnapshot,
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
const dayNutrition = ref<DietNutritionSnapshot | null>(null)
const nutritionTarget = ref<DietNutritionSnapshot | null>(null)

// Food Dialog
const foodDialogVisible = ref(false)
const foodKeyword = ref('')
const foodList = ref<FoodResp[]>([])
const foodLoading = ref(false)

async function openFoodDialog() {
  foodDialogVisible.value = true
  foodKeyword.value = ''
  foodList.value = []
  // Initial load
  await searchFood()
}

async function searchFood() {
  foodLoading.value = true
  try {
    const res = await apiGetFoods({ keyword: foodKeyword.value, page: 1, size: 20 })
    foodList.value = normalizeFoodList(unwrapApi<any>(res))
  } finally {
    foodLoading.value = false
  }
}

function normalizeFoodList(list: any[]): FoodResp[] {
  if (!Array.isArray(list)) return []
  return list.map(x => ({
    id: x.id,
    foodName: x.foodName,
    brand: x.brand,
    unit: x.unit,
    kcal: x.kcal,
    proteinG: x.proteinG,
    carbG: x.carbG,
    fatG: x.fatG,
    fiberG: x.fiberG,
    sodiumMg: x.sodiumMg
  }))
}

// Update a row with selected food data
function updateRowWithFood(row: ExtendedItem, food: FoodResp) {
  row.foodId = food.id
  row.foodName = food.foodName
  row.brand = food.brand || ''
  
  // Smart Unit Logic
  // If unit is "100g" or "100ml", we set unit to "g"/"ml" and amount to 100.
  // And we record the base values.
  let baseAmount = 1
  let targetUnit = food.unit || 'g'
  
  if (food.unit === '100g') {
    targetUnit = 'g'
    baseAmount = 100
  } else if (food.unit === '100ml') {
    targetUnit = 'ml'
    baseAmount = 100
  } else {
    // For other units (e.g. "碗(150g)", "个"), we assume the nutrition is PER that unit.
    // So base amount is 1.
    baseAmount = 1
  }

  row.unit = targetUnit
  row.amount = baseAmount
  
  // Set nutrition values
  row.kcal = Math.round(food.kcal)
  row.proteinG = Number(food.proteinG || 0)
  row.carbG = Number(food.carbG || 0)
  row.fatG = Number(food.fatG || 0)
  row.fiberG = Number(food.fiberG || 0)
  row.sodiumMg = Number(food.sodiumMg || 0)

  // Save base for auto-calc
  row._base = {
    amount: baseAmount,
    kcal: row.kcal,
    proteinG: row.proteinG,
    carbG: row.carbG,
    fatG: row.fatG
  }
}

function selectFood(food: FoodResp) {
  // Add new row
  const row: ExtendedItem = {
    foodName: '', brand: '', amount: 0, unit: 'g',
    kcal: 0, proteinG: 0, carbG: 0, fatG: 0
  }
  items.push(row)
  updateRowWithFood(row, food)
  foodDialogVisible.value = false
}

// Autocomplete helpers
const foodSuggestions = async (qs: string, cb: any) => {
  if (!qs) {
    cb([])
    return
  }
  const res = await apiGetFoods({ keyword: qs, page: 1, size: 10 })
  const list = normalizeFoodList(unwrapApi<any>(res))
  // map to value/link for element? No, element autocomplete needs `value` property to show text?
  // Or we can use custom template. We used custom template in previous code?
  // Looking at template: <el-autocomplete ... @select="(it) => applyFoodToRow(row, it.food)" />
  // We need to return objects with `value` (for display) and `food` (data).
  const suggestions = list.map(f => ({
    value: f.foodName + (f.brand ? ` (${f.brand})` : ''),
    food: f
  }))
  cb(suggestions)
}

function applyFoodToRow(row: ExtendedItem, food: FoodResp) {
  updateRowWithFood(row, food)
}

// Watch for amount changes to auto-calc nutrition
watch(() => items, (newItems) => {
  for (const row of newItems) {
    if (row._base && row.amount !== row._base.amount) {
      // Calculate ratio
      // Avoid division by zero
      if (row._base.amount <= 0) continue
      
      const ratio = row.amount / row._base.amount
      
      row.kcal = Math.round(row._base.kcal * ratio)
      row.proteinG = Number((row._base.proteinG * ratio).toFixed(1))
      row.carbG = Number((row._base.carbG * ratio).toFixed(1))
      row.fatG = Number((row._base.fatG * ratio).toFixed(1))
      // Optional: Update others if needed
    }
  }
}, { deep: true })

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


function applyLogToForm(log: DietLogResp) {
  currentId.value = log.id
  items.splice(0, items.length)
  for (const it of log.items || []) {
    const row: ExtendedItem = {
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
    }
    // If we want to enable auto-calc for existing items, we would need to know their base values.
    // But we don't store base values in DB.
    // We could try to fetch food info, but that's too heavy.
    // So for loaded items, auto-calc is disabled unless user re-selects food.
    items.push(row)
  }
  if (items.length === 0) addRow()
}

function resetToNew() {
  currentId.value = null
  items.splice(0, items.length)
  addRow()
}

// Unified load function to avoid duplicate requests
async function loadDayData() {
  const date = toYmd(form.date)
  loading.value = true
  loadingNutrition.value = true
  
  try {
    // 1. Get Day Summary (includes meals list, day total, and target)
    const res = await apiGetDietLogsByDay(date)
    const data = unwrapApi<DietLogDayResp>(res)
    
    if (data) {
      // Update Nutrition Summary
      dayNutrition.value = data.dayTotal
      nutritionTarget.value = data.target || null
      
      // 2. Check for existing meal
      const hit = (data.meals || []).find(x => x.mealType === form.mealType)
      if (hit) {
        // Fetch full detail
        const detailRes = await apiGetDietLogById(hit.id)
        const detail = unwrapApi<DietLogResp>(detailRes)
        applyLogToForm(detail)
      } else {
        resetToNew()
      }
    } else {
      resetToNew()
      dayNutrition.value = null
    }
  } catch (e: any) {
    console.error(e)
    resetToNew()
  } finally {
    loading.value = false
    loadingNutrition.value = false
  }
}

// Deprecated separate loaders (kept for compatibility if referenced, but mapped to new logic)
async function loadTodayNutrition() {
  // no-op, handled by loadDayData
}

async function reload() {
  await loadDayData()
}

watch(
  () => [toYmd(form.date), form.mealType],
  async () => {
    await loadDayData()
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
