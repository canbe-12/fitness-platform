<template>
  <div class="menu-wrap">
    <div class="brand">
      <div class="title">智能健身助手</div>
      <div class="sub">{{ auth.displayName }}</div>
    </div>

    <el-menu
      :default-active="active"
      class="el-menu-vertical"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-menu-item>

      <el-menu-item index="/workout/submit">
        <el-icon><Football /></el-icon>
        <span>训练执行</span>
      </el-menu-item>

      <el-menu-item index="/diet/submit">
        <el-icon><Dish /></el-icon>
        <span>饮食记录</span>
      </el-menu-item>

      <el-menu-item index="/report/weekly">
        <el-icon><Document /></el-icon>
        <span>训练报告</span>
      </el-menu-item>

      <el-menu-item index="/plan">
        <el-icon><Calendar /></el-icon>
        <span>我的计划</span>
      </el-menu-item>

      <el-menu-item index="/body/trend">
        <el-icon><TrendCharts /></el-icon>
        <span>身体趋势</span>
      </el-menu-item>

      <div class="menu-bottom">
        <el-menu-item index="/settings/profile">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </el-menu-item>

        <el-menu-item @click="onLogout" index="__logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </div>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'
import {
  House, Football, Dish, Document, Calendar, TrendCharts, Setting, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)

async function onLogout() {
  const ok = await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning',
    confirmButtonText: '退出',
    cancelButtonText: '取消',
  }).then(() => true).catch(() => false)

  if (!ok) return
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.menu-wrap { height: 100%; display: flex; flex-direction: column; }
.brand {
  padding: 18px 16px 10px;
  border-bottom: 1px solid #eef2f7;
}
.title { font-size: 18px; font-weight: 800; }
.sub { font-size: 12px; color: var(--muted); margin-top: 6px; }

.el-menu-vertical { border-right: none; flex: 1; }

.menu-bottom {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid #eef2f7;
}
</style>
