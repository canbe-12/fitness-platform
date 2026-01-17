<template>
  <el-card class="qcard" shadow="never" @click="$emit('click')">
    <div class="row">
      <img v-if="icon" :src="icon" alt="" class="icon" />
      <div class="content">
        <div class="t">{{ title }}</div>
        <div class="d">{{ desc }}</div>
      </div>
    </div>
  </el-card>
  </template>

<script setup lang="ts">
defineProps<{ title: string; desc: string; icon?: string }>()
defineEmits<{ (e: 'click'): void }>()
</script>

<style scoped lang="scss">
.qcard {
  border-radius: 18px;
  cursor: pointer;
  transition: transform .08s ease;
}
.qcard:hover { transform: translateY(-2px); }
.row { display: flex; align-items: center; gap: 14px; }
.icon { width: 40px; height: 40px; filter: drop-shadow(0 4px 10px rgba(34,211,238,.25)); }
.content { display: flex; flex-direction: column; }
.t { font-size: 18px; font-weight: 800; }
.d { margin-top: 4px; color: var(--muted); }

@media (max-width: 720px) {
  .icon { width: 32px; height: 32px; }
}
</style>
