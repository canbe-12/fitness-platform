<template>
  <label class="field">
    <div class="label">{{ label }}</div>
    <slot />
    <div v-if="hint" class="hint">{{ hint }}</div>
  </label>
</template>

<script setup lang="ts">
defineProps<{ label: string; hint?: string }>();
</script>

<style scoped>
.field { display: block; margin-bottom: 12px; }
.label { font-size: 13px; color: #374151; margin-bottom: 6px; }
.hint { font-size: 12px; color: #6b7280; margin-top: 6px; }
</style>
