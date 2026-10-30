<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Tag from 'primevue/tag';
import { getNotifications } from '@/service/api.js';

const notifications = ref([]);
const error = ref(false);
const loading = ref(true);
let active = true;

function statusLabel(status) {
    if (status === 'sent') return 'Отправлено';
    if (status === 'failed') return 'Ошибка отправки';
    return status || 'Статус не указан';
}

function statusSeverity(status) {
    if (status === 'sent') return 'success';
    if (status === 'failed') return 'danger';
    return 'secondary';
}

onMounted(async () => {
    try {
        const result = await getNotifications();
        if (active) notifications.value = result;
    } catch {
        if (active) {
            notifications.value = [];
            error.value = true;
        }
    } finally {
        if (active) loading.value = false;
    }
});

onUnmounted(() => {
    active = false;
});
</script>

<template>
    <div class="card">
        <h1>Уведомления</h1>
        <p class="text-sm text-surface-500 mb-4">Сервер возвращает не более 20 последних записей.</p>
        <p v-if="loading" role="status">Загрузка уведомлений...</p>
        <p v-else-if="error" role="alert">Не удалось загрузить уведомления. Проверьте соединение и обновите страницу.</p>
        <p v-else-if="!notifications.length" role="status">Уведомлений пока нет.</p>
        <DataTable v-else :value="notifications" dataKey="id" scrollable tableStyle="min-width: 42rem">
            <Column field="sent_at" header="Время" style="min-width: 12rem">
                <template #body="{ data }">{{ data.sent_at || 'Время не указано' }}</template>
            </Column>
            <Column field="message" header="Сообщение" style="min-width: 20rem">
                <template #body="{ data }">{{ data.message || 'Сообщение отсутствует' }}</template>
            </Column>
            <Column field="status" header="Статус" style="min-width: 10rem">
                <template #body="{ data }"><Tag :value="statusLabel(data.status)" :severity="statusSeverity(data.status)" /></template>
            </Column>
        </DataTable>
    </div>
</template>
