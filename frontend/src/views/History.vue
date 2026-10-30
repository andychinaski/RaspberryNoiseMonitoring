<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Tag from 'primevue/tag';
import { getEvents } from '@/service/api.js';

const events = ref([]);
const error = ref(false);
let active = true;

function eventSeverity(type) {
    return { NORMAL: 'success', WARNING: 'warn', CRITICAL: 'danger' }[type] || 'secondary';
}

onMounted(async () => {
    try {
        const result = await getEvents();
        if (active) events.value = result;
    } catch {
        if (active) {
            events.value = [];
            error.value = true;
        }
    }
});

onUnmounted(() => {
    active = false;
});
</script>

<template>
    <div class="card">
        <h1>История</h1>
        <p class="text-sm text-surface-500 mb-4">Сервер возвращает не более 20 последних записей.</p>
        <p v-if="error" role="alert">Не удалось загрузить историю. Проверьте соединение и обновите страницу.</p>
        <DataTable v-if="!error && events.length" :value="events" dataKey="id" scrollable tableStyle="min-width: 48rem">
            <Column field="timestamp" header="Время" style="min-width: 12rem" />
            <Column field="noise_level" header="Уровень шума" style="min-width: 10rem">
                <template #body="{ data }">{{ data.noise_level }} дБ</template>
            </Column>
            <Column field="type" header="Тип события" style="min-width: 11rem">
                <template #body="{ data }"><Tag :value="data.type" :severity="eventSeverity(data.type)" /></template>
            </Column>
            <Column field="info" header="Описание" style="min-width: 15rem">
                <template #body="{ data }">{{ data.info || 'Нет описания' }}</template>
            </Column>
        </DataTable>
    </div>
</template>
