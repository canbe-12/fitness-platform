<template>
  <div class="ring">
    <el-progress type="dashboard" :percentage="percent" />
    <div class="label">{{ title }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; percent: number }>()
</script>

<style scoped lang="scss">
.ring { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.label {
  background: #eef2f7;
  padding: 6px 14px;
  border-radius: 999px;
  font-weight: 700;
  color: #475569;
}
</style>
