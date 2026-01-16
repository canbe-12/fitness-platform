<template>
  <el-dialog v-model="visible" title="选择食物" width="980px">
    <div class="top">
      <el-input
        v-model="keyword"
        placeholder="搜索食物名称/品牌"
        clearable
        @keyup.enter="load"
        style="width: 320px"
      />
      <el-button type="primary" :loading="loading" @click="load">搜索</el-button>

      <div class="spacer"></div>

      <el-button plain @click="emit('create')">新增食物</el-button>
    </div>

    <el-table :data="rows" border style="width:100%;" v-loading="loading">
      <el-table-column prop="foodName" label="食物" min-width="180" />
      <el-table-column prop="brand" label="品牌" min-width="140" />
      <el-table-column prop="unit" label="单位" width="90" />
      <el-table-column prop="kcal" label="kcal" width="90" />
      <el-table-column prop="proteinG" label="P(g)" width="90" />
      <el-table-column prop="carbG" label="C(g)" width="90" />
      <el-table-column prop="fatG" label="F(g)" width="90" />

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="pick(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" v-if="showPager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="prev, pager, next, sizes, total"
        :total="total"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { apiGetFoods, type FoodResp } from '@/api/food'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'select', food: FoodResp): void
  (e: 'create'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v),
})

const loading = ref(false)
const keyword = ref('')
const rows = ref<FoodResp[]>([])

const page = ref(1)
const size = ref(10)
const total = ref(0)

const showPager = computed(() => total.value > 0)

function normalizeList(data: any) {
  // 兼容：数组 / {records}/ {list}
  if (Array.isArray(data)) return { list: data, total: data.length }
  const list = data?.records ?? data?.list ?? []
  const t = Number(data?.total ?? list.length)
  return { list, total: t }
}

async function load() {
  loading.value = true
  try {
    const res = await apiGetFoods({
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value,
    })
    const { list, total: t } = normalizeList(res.data.data)
    rows.value = list
    total.value = t
  } finally {
    loading.value = false
  }
}

function onSizeChange() {
  page.value = 1
  load()
}

function pick(food: FoodResp) {
  emit('select', food)
  visible.value = false
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) load()
  }
)
</script>

<style scoped lang="scss">
.top { display:flex; gap:10px; align-items:center; margin-bottom:12px; }
.spacer { flex:1; }
.pager { display:flex; justify-content:flex-end; margin-top:12px; }
</style>
