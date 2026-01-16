<template>
  <el-card class="qcard" shadow="never" @click="$emit('click')">
    <div class="t">{{ title }}</div>
    <div class="d">{{ desc }}</div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{ title: string; desc: string }>()
defineEmits<{ (e: 'click'): void }>()
</script>

<style scoped lang="scss">
.qcard {
  border-radius: 18px;
  cursor: pointer;
  transition: transform .08s ease;
}
.qcard:hover { transform: translateY(-2px); }
.t { font-size: 20px; font-weight: 900; }
.d { margin-top: 6px; color: var(--muted); }
</style>
