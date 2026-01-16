<template>
  <el-card shadow="never" class="card">
    <template #header>
      <div class="h">
        <div class="title">记录饮食</div>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </div>
    </template>

    <el-form label-position="top">
      <el-row :gutter="12">
        <el-col :span="6">
          <el-form-item label="日期">
            <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item label="餐次">
            <el-select v-model="mealType" style="width:100%;">
              <el-option label="早餐" value="BREAKFAST" />
              <el-option label="午餐" value="LUNCH" />
              <el-option label="晚餐" value="DINNER" />
              <el-option label="加餐" value="SNACK" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="快速操作">
            <div class="tools">
              <el-button @click="addRow">添加一条</el-button>
              <el-button plain @click="openPickerForNew">从食物库选择</el-button>
              <el-button plain @click="clearRows">清空</el-button>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table :data="items" border style="width:100%;" class="tbl">
      <el-table-column label="食物" min-width="220">
        <template #default="{ row }">
          <div class="foodcell">
            <el-input v-model="row.foodName" placeholder="食物名称" />
            <el-button plain @click="openPicker(row)">选择</el-button>
          </div>
          <div class="muted" v-if="row.brand">品牌：{{ row.brand }}</div>
        </template>
      </el-table-column>

      <el-table-column label="数量" width="170">
        <template #default="{ row }">
          <div class="qty">
            <el-input-number v-model="row.amount" :min="0" :step="10" style="width:110px;" />
            <el-input v-model="row.unit" placeholder="单位" style="width:70px;" />
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="kcal" label="kcal" width="110">
        <template #default="{ row }">
          <el-input-number v-model="row.kcal" :min="0" :step="10" style="width:100%;" />
        </template>
      </el-table-column>

      <el-table-column label="P/C/F(g)" width="260">
        <template #default="{ row }">
          <div class="pcf">
            <el-input-number v-model="row.proteinG" :min="0" :step="1" style="width:80px;" />
            <el-input-number v-model="row.carbG" :min="0" :step="1" style="width:80px;" />
            <el-input-number v-model="row.fatG" :min="0" :step="1" style="width:80px;" />
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ $index }">
          <el-button type="danger" link @click="removeRow($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="sum">
      <div class="kv"><span>总 kcal</span><b>{{ sums.kcal }}</b></div>
      <div class="kv"><span>蛋白(g)</span><b>{{ sums.proteinG }}</b></div>
      <div class="kv"><span>碳水(g)</span><b>{{ sums.carbG }}</b></div>
      <div class="kv"><span>脂肪(g)</span><b>{{ sums.fatG }}</b></div>
    </div>

    <FoodPickerModal v-model="pickerVisible" @select="onPick" />
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import FoodPickerModal from './FoodPickerModal.vue'
import { apiCreateDietLog, type DietLogItemReq, type MealType } from '@/api/diet'
import type { FoodResp } from '@/api/food'
import { newClientId } from '@/utils/id'
import { toYmd } from '@/utils/date'

const submitting = ref(false)
const date = ref(toYmd(new Date()))
const mealType = ref<MealType>('BREAKFAST')

// DietLogItemReq：你之前 diet.ts 里已有
const items = reactive<DietLogItemReq[]>([
  {
    foodId: 0,
    foodName: '',
    brand: '',
    amount: 100,
    unit: 'g',
    kcal: 0,
    proteinG: 0,
    carbG: 0,
    fatG: 0,
    fiberG: 0,
    sodiumMg: 0,
  },
])

function addRow() {
  items.push({
    foodId: 0,
    foodName: '',
    brand: '',
    amount: 100,
    unit: 'g',
    kcal: 0,
    proteinG: 0,
    carbG: 0,
    fatG: 0,
    fiberG: 0,
    sodiumMg: 0,
  })
}

function removeRow(i: number) {
  items.splice(i, 1)
}

function clearRows() {
  items.splice(0, items.length)
  addRow()
}

// --- 食物选择 ---
const pickerVisible = ref(false)
let targetRow: DietLogItemReq | null = null

function openPicker(row: DietLogItemReq) {
  targetRow = row
  pickerVisible.value = true
}

function openPickerForNew() {
  const r: DietLogItemReq = {
    foodId: 0,
    foodName: '',
    brand: '',
    amount: 100,
    unit: 'g',
    kcal: 0,
    proteinG: 0,
    carbG: 0,
    fatG: 0,
    fiberG: 0,
    sodiumMg: 0,
  }
  items.push(r)
  openPicker(r)
}

function onPick(food: FoodResp) {
  if (!targetRow) return
  targetRow.foodId = food.id
  targetRow.foodName = food.foodName
  targetRow.brand = food.brand || ''
  targetRow.unit = food.unit || targetRow.unit || 'g'

  // 这里按“食物库给的就是一份/单位的营养值”直接回填
  // 如果你的后端定义是“每100g”，你可以在这里按 amount 比例换算
  targetRow.kcal = Number(food.kcal || 0)
  targetRow.proteinG = Number(food.proteinG || 0)
  targetRow.carbG = Number(food.carbG || 0)
  targetRow.fatG = Number(food.fatG || 0)
  targetRow.fiberG = Number(food.fiberG || 0)
  targetRow.sodiumMg = Number(food.sodiumMg || 0)
}

// --- 汇总 ---
const sums = computed(() => {
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

async function submit() {
  // 基本校验
  const valid = items.filter(i => i.foodName?.trim())
  if (valid.length === 0) {
    ElMessage.warning('请至少填写一条食物（或从食物库选择）')
    return
  }

  submitting.value = true
  try {
    const payload = {
      clientRequestId: newClientId(),
      date: date.value,
      mealType: mealType.value,
      items: valid.map(v => ({
        foodId: v.foodId || 0,
        foodName: v.foodName,
        brand: v.brand || '',
        amount: Number(v.amount || 0),
        unit: v.unit || 'g',
        kcal: Number(v.kcal || 0),
        proteinG: Number(v.proteinG || 0),
        carbG: Number(v.carbG || 0),
        fatG: Number(v.fatG || 0),
        fiberG: Number(v.fiberG || 0),
        sodiumMg: Number(v.sodiumMg || 0),
      })),
    }
    const res = await apiCreateDietLog(payload as any)
    ElMessage.success(`提交成功：id=${res.data.data.id}`)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.card { border-radius: 18px; }
.h { display:flex; align-items:center; justify-content:space-between; gap:12px; }
.title { font-size:18px; font-weight:900; }
.tbl { border-radius: 12px; overflow:hidden; }
.tools { display:flex; gap:10px; flex-wrap:wrap; }

.foodcell { display:flex; gap:10px; align-items:center; }
.qty { display:flex; gap:8px; align-items:center; }
.pcf { display:flex; gap:10px; align-items:center; }
.muted { color: var(--muted); font-size:12px; margin-top:6px; }

.sum {
  display:grid;
  grid-template-columns: repeat(4, 1fr);
  gap:10px;
  margin-top:12px;
}
.kv { background:#f3f6fb; border-radius:12px; padding:10px; display:flex; flex-direction:column; gap:6px; }
.kv span { color: var(--muted); font-size:12px; }
.kv b { font-size:18px; }
</style>
