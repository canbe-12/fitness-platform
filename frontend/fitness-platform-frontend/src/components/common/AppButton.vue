<template>
  <button class="btn" :type="type" :disabled="disabled">
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{ type?: "button" | "submit"; disabled?: boolean }>();
</script>

<style scoped>
.btn {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #111827;
  color: white;
  cursor: pointer;
}
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
