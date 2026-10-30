<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import DatePicker from 'primevue/datepicker';
import Tag from 'primevue/tag';
import ToggleSwitch from 'primevue/toggleswitch';
import { getEvents } from '@/service/api.js';

const events = ref([]);
const error = ref(false);
const loading = ref(true);
const selectedDate = ref(null);
const onlyCritical = ref(false);
let active = true;
let requestId = 0;

function eventSeverity(type) {
    return { NORMAL: 'success', WARNING: 'warn', CRITICAL: 'danger' }[type] || 'secondary';
}

function formatDate(date) {
    return date ? [date.getFullYear(), String(date.getMonth() + 1).padStart(2, '0'), String(date.getDate()).padStart(2, '0')].join('-') : undefined;
}

async function refresh() {
    const id = ++requestId;
    events.value = [];
    error.value = false;
    loading.value = true;
    try {
        const result = await getEvents(formatDate(selectedDate.value), onlyCritical.value || undefined);
        if (active && id === requestId) events.value = result;
    } catch {
        if (active && id === requestId) {
            events.value = [];
            error.value = true;
        }
    } finally {
        if (active && id === requestId) loading.value = false;
    }
}

watch([selectedDate, onlyCritical], refresh, { flush: 'sync' });
onMounted(refresh);
onUnmounted(() => {
    active = false;
    requestId++;
});
</script>

<template>
    <div class="card">
        <h1>История</h1>
        <p class="text-sm text-surface-500 mb-4">Сервер возвращает не более 20 последних записей.</p>
        <div class="flex flex-wrap items-end gap-4 mb-6">
            <div class="flex flex-col gap-2">
                <label for="history-date">Дата</label>
                <DatePicker v-model="selectedDate" inputId="history-date" dateFormat="dd.mm.yy" placeholder="Все даты" showIcon :manualInput="false" />
            </div>
            <Button v-if="selectedDate" label="Все даты" severity="secondary" outlined @click="selectedDate = null" />
            <div class="flex items-center gap-2 pb-2">
                <ToggleSwitch v-model="onlyCritical" inputId="history-critical" />
                <label for="history-critical">Только превышения</label>
            </div>
        </div>
        <p v-if="loading" role="status">Загрузка истории...</p>
        <div v-else-if="error" class="flex flex-col items-start gap-3">
            <p style="margin: 0" role="alert">Не удалось загрузить историю. Проверьте соединение и попробуйте снова.</p>
            <Button label="Повторить" icon="pi pi-refresh" @click="refresh" />
        </div>
        <p v-else-if="!events.length" role="status">Нет записей для выбранных фильтров.</p>
        <DataTable v-else :value="events" dataKey="id" scrollable tableStyle="min-width: 48rem">
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
