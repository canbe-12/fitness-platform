<template>
  <el-card shadow="never" class="card">
    <template #header>
      <div class="h">
        <div class="title">设置</div>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </template>

    <el-form label-position="top" :model="form">
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="性别">
            <el-select v-model="form.gender" style="width:100%;">
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="生日">
            <el-date-picker v-model="form.birthday" type="date" style="width:100%;" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="身高(cm)">
            <el-input-number v-model="form.heightCm" :min="0" style="width:100%;" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="体重(kg)">
            <el-input-number v-model="form.weightKg" :min="0" :step="0.1" style="width:100%;" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="目标（可选）">
            <el-input v-model="form.goal" placeholder="例如：减脂/增肌/维持" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="水平（可选）">
            <el-input v-model="form.level" placeholder="例如：新手/进阶" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiMe, apiUpdateMe } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const saving = ref(false)

const form = reactive({
  nickname: '',
  gender: 'MALE',
  birthday: '',
  heightCm: 0,
  weightKg: 0,
  goal: '',
  level: '',
})

async function load() {
  const res = await apiMe()
  const me = res.data.data
  form.nickname = me.nickname || ''
  form.gender = (me.gender as any) || 'MALE'
  form.birthday = me.birthday || ''
  form.heightCm = me.heightCm || 0
  form.weightKg = me.weightKg || 0
  form.goal = me.goal || ''
  form.level = me.level || ''
}

async function save() {
  saving.value = true
  try {
    const res = await apiUpdateMe({
      nickname: form.nickname || undefined,
      gender: form.gender || undefined,
      birthday: form.birthday || undefined,
      heightCm: form.heightCm || undefined,
      weightKg: form.weightKg || undefined,
      goal: form.goal || undefined,
      level: form.level || undefined,
    })
    ElMessage.success('保存成功')
    // 更新 store 中展示名
    auth.me = res.data.data as any
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.card { border-radius: 18px; }
.h { display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 18px; font-weight: 900; }
</style>
