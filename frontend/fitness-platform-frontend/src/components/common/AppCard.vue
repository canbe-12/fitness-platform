<template>
  <div class="card">
    <div v-if="title" class="card-title">{{ title }}</div>
    <div class="card-body"><slot /></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title?: string }>();
</script>

<style scoped>
.card { border: 1px solid #e5e7eb; border-radius: 12px; padding: 16px; background: #fff; }
.card-title { font-weight: 700; margin-bottom: 12px; }
</style>
