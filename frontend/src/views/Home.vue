<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { getNoiseStats } from '@/service/noiseStats.js';

const now = new Date();
const localDate = [now.getFullYear(), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')].join('-');
const selectedDate = ref(localDate);
const stats = ref(null);
const loading = ref(false);
const error = ref(false);
let requestId = 0;

async function refresh() {
    const id = ++requestId;
    stats.value = null;
    error.value = false;
    if (!selectedDate.value) {
        loading.value = false;
        return;
    }
    loading.value = true;
    try {
        const result = await getNoiseStats(selectedDate.value);
        if (id === requestId) stats.value = result;
    } catch {
        if (id === requestId) error.value = true;
    } finally {
        if (id === requestId) loading.value = false;
    }
}

watch(selectedDate, refresh, { flush: 'sync' });
onMounted(refresh);
onUnmounted(() => requestId++);
</script>

<template>
    <div class="card">
        <h1>Мониторинг</h1>
        <div class="flex flex-wrap items-end gap-4 my-4">
            <label class="flex flex-col gap-2" for="monitoring-date">Дата
                <input id="monitoring-date" v-model="selectedDate" type="date" class="border rounded p-2" />
            </label>
            <button type="button" class="border rounded px-4 py-2" :disabled="!selectedDate" @click="refresh">Обновить</button>
        </div>
        <p v-if="!selectedDate" role="status">Выберите дату.</p>
        <p v-else-if="loading" role="status">Загрузка статистики...</p>
        <p v-else-if="error" role="alert">Не удалось загрузить статистику. Проверьте соединение и попробуйте снова.</p>
        <p v-else-if="stats && stats.current_timestamp == null" role="status">За выбранную дату измерений нет.</p>
        <dl v-else-if="stats" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            <div><dt>Последнее измерение выбранного дня</dt><dd class="text-xl font-semibold">{{ stats.current_noise }} дБ</dd></div>
            <div><dt>Время последнего измерения</dt><dd class="text-xl font-semibold">{{ stats.current_timestamp }}</dd></div>
            <div><dt>Минимум за день</dt><dd class="text-xl font-semibold">{{ stats.min_noise }} дБ</dd></div>
            <div><dt>Максимум за день</dt><dd class="text-xl font-semibold">{{ stats.max_noise }} дБ</dd></div>
            <div><dt>Тип события</dt><dd class="text-xl font-semibold">{{ stats.event_type }}</dd></div>
            <div><dt>Отправлено уведомлений</dt><dd class="text-xl font-semibold">{{ stats.notifications_sent }}</dd></div>
        </dl>
    </div>
</template>
