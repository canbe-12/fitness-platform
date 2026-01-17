<template>
  <AppCard title="提交训练记录">
    <form @submit.prevent="onSubmit">
      <AppFormField label="归属日期（planned_date）">
        <input v-model="date" class="input" placeholder="2026-01-05" />
      </AppFormField>

      <div class="row">
        <div class="col">
          <AppFormField label="Exercise ID">
            <input v-model.number="exerciseId" class="input" type="number" />
          </AppFormField>
        </div>
        <div class="col">
          <AppFormField label="Actual Weight">
            <input v-model.number="actualWeight" class="input" type="number" step="0.5" />
          </AppFormField>
        </div>
        <div class="col">
          <AppFormField label="Actual Reps">
            <input v-model.number="actualReps" class="input" type="number" />
          </AppFormField>
        </div>
      </div>

      <AppFormField label="备注（可选）">
        <input v-model="notes" class="input" />
      </AppFormField>

      <div class="actions">
        <AppButton type="submit">提交</AppButton>
      </div>

      <pre class="result" v-if="result">{{ result }}</pre>
    </form>
  </AppCard>
</template>

<script setup lang="ts">
import { ref } from "vue";
import AppCard from "@/components/common/AppCard.vue";
import AppFormField from "@/components/common/AppFormField.vue";
import AppButton from "@/components/common/AppButton.vue";
import { submitExerciseLog } from "@/api/workout";

function uuid() {
  // 简易 uuid（够课程项目用）；你也可以换成 crypto.randomUUID()
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

const date = ref("2026-01-05");
const exerciseId = ref(1);
const actualWeight = ref(20);
const actualReps = ref(10);
const notes = ref("");

const result = ref("");

async function onSubmit() {
  const payload = {
    clientRequestId: uuid(),
    date: date.value,
    logs: [
      {
        exerciseId: exerciseId.value,
        actualWeight: actualWeight.value,
        actualReps: actualReps.value,
        notes: notes.value || undefined,
      },
    ],
  };
  const resp = await submitExerciseLog(payload);
  result.value = JSON.stringify(resp, null, 2);
}
</script>

<style scoped>
.input { width: 100%; padding: 10px; border: 1px solid #e5e7eb; border-radius: 10px; }
.row { display: flex; gap: 10px; }
.col { flex: 1; }
.actions { margin-top: 12px; }
.result { margin-top: 12px; background: #1e293b; color: #f8fafc; padding: 12px; border-radius: 10px; overflow: auto; }
</style>
