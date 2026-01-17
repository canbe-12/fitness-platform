<template>
  <!-- 通用按钮：支持 variant 与 size，提升交互与视觉效果 -->
  <button
    class="btn"
    :data-variant="variant"
    :data-size="size"
    :type="type"
    :disabled="disabled"
  >
    <slot />
  </button>
  </template>

<script setup lang="ts">
defineProps<{
  type?: "button" | "submit";
  disabled?: boolean;
  variant?: "primary" | "outline" | "ghost";
  size?: "sm" | "md" | "lg";
}>();
</script>

<style scoped>
/* 现代化按钮样式 */
.btn {
  --bg: var(--primary-600);
  --bg-hover: var(--primary-700);
  --text: #fff;
  --border: transparent;
  --radius: 12px;
  --pad-y: 10px;
  --pad-x: 16px;
  --shadow: 0 10px 26px rgba(34, 211, 238, .18);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: var(--pad-y) var(--pad-x);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  background: var(--bg);
  color: var(--text);
  cursor: pointer;
  box-shadow: var(--shadow);
  will-change: transform;
}
.btn:hover { background: var(--bg-hover); transform: translateY(-1px); }
.btn:active { transform: translateY(0); }
.btn:disabled { opacity: 0.6; cursor: not-allowed; box-shadow: none; }

/* 尺寸变体 */
.btn[data-size="sm"] { --pad-y: 8px; --pad-x: 12px; --radius: 10px; font-size: 14px; }
.btn[data-size="md"] { font-size: 15px; }
.btn[data-size="lg"] { --pad-y: 12px; --pad-x: 18px; --radius: 14px; font-weight: 700; }

/* 外观变体 */
.btn[data-variant="outline"] {
  --bg: transparent;
  --bg-hover: rgba(0,0,0,.04);
  --text: var(--text);
  --border: rgba(0,0,0,.12);
  box-shadow: none;
}
.btn[data-variant="ghost"] {
  --bg: transparent;
  --bg-hover: rgba(0,0,0,.04);
  --text: var(--text);
  --border: transparent;
  box-shadow: none;
}
</style>
