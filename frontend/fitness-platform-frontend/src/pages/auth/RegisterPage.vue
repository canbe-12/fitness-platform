<template>
  <div class="auth-wrap">
    <el-card class="card">
      <div class="title">注册</div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="昵称（可选）" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" class="btn" @click="onSubmit">
          注册
        </el-button>

        <div class="link">
          已有账号？
          <el-link type="primary" @click="router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await auth.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined,
    })
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-wrap { height: 100%; display: flex; align-items: center; justify-content: center; }
.card { width: 420px; border-radius: 14px; }
.title { font-size: 22px; font-weight: 800; margin-bottom: 14px; }
.btn { width: 100%; margin-top: 8px; }
.link { margin-top: 12px; font-size: 13px; color: var(--muted); }
</style>
