<template>
  <div class="topbar">
    <div class="slogan">每一次进步，都是向目标更近一步</div>
    <div class="right">
      <el-tag type="info" effect="plain">{{ auth.displayName }}</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>

<style scoped lang="scss">
.topbar {
  position: relative;
  width: 100%;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  box-sizing: border-box;
}

.slogan {
  font-size: 32px;
  font-weight: 900;
  letter-spacing: 1px;
  color: #1e3a8a;
  text-align: center;
  line-height: 1;
  text-shadow: 0 1px 2px rgba(0,0,0,.08);
}

.right {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
}
</style>
