<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import Chart from 'primevue/chart';
import { useLayout } from '@/layout/composables/layout';
import { getNoiseStats } from '@/service/noiseStats.js';
import { apiUrl } from '@/service/api.js';

const now = new Date();
const localDate = [now.getFullYear(), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')].join('-');
const selectedDate = ref(localDate);
const stats = ref(null);
const loading = ref(false);
const error = ref(false);
const thresholds = ref(null);
let thresholdRequestId = 0;
const { layoutConfig, isDarkTheme } = useLayout();
const chartColor = ref('#10b981');
const chartFill = ref('rgba(16, 185, 129, 0.18)');
function updateChartTheme() {
    const style = getComputedStyle(document.documentElement);
    chartColor.value = style.getPropertyValue('--p-primary-color').trim() || '#10b981';
    chartFill.value = style.getPropertyValue('--p-primary-100').trim() || 'rgba(16, 185, 129, 0.18)';
}
watch([() => layoutConfig.primary, () => layoutConfig.preset, () => layoutConfig.surface, isDarkTheme], async () => {
    await nextTick();
    updateChartTheme();
});
const chartPoints = computed(() => stats.value?.last_10_minutes ?? []);
const chartData = computed(() => ({
    labels: chartPoints.value.map((point) => point.timestamp),
    datasets: [
        { label: 'Уровень шума, дБ', data: chartPoints.value.map((point) => point.noise_level), borderColor: chartColor.value, backgroundColor: chartFill.value, fill: true, borderWidth: 2, pointRadius: 0, pointHitRadius: 10, pointHoverRadius: 4, tension: 0.15 },
        ...(thresholds.value ? [
            { label: 'Предупреждение', data: chartPoints.value.map(() => thresholds.value.warning), borderColor: '#f59e0b', borderDash: [6, 4], borderWidth: 1.5, pointRadius: 0, pointHitRadius: 0 },
            { label: 'Критический', data: chartPoints.value.map(() => thresholds.value.critical), borderColor: '#ef4444', borderDash: [6, 4], borderWidth: 1.5, pointRadius: 0, pointHitRadius: 0 }
        ] : [])
    ]
}));
const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    plugins: { legend: { display: false }, tooltip: { filter: (item) => item.datasetIndex === 0, callbacks: { label: (context) => `${context.parsed.y} дБ` } } },
    scales: {
        x: { grid: { display: false }, ticks: { maxTicksLimit: 5, maxRotation: 0, callback(value) { return String(this.getLabelForValue(value)).slice(11, 16); } } },
        y: { beginAtZero: true, title: { display: true, text: 'дБ' }, ticks: { maxTicksLimit: 5 }, grid: { color: 'rgba(128, 128, 128, 0.12)' } }
    }
};
let requestId = 0;

async function refresh() {
    const id = ++requestId;
    const thresholdId = ++thresholdRequestId;
    thresholds.value = null;
    stats.value = null;
    error.value = false;
    if (!selectedDate.value) {
        loading.value = false;
        return;
    }
    fetch(apiUrl('/api/device-info'))
        .then((response) => { if (!response.ok) throw new Error('Ошибка получения порогов'); return response.json(); })
        .then((device) => {
            if (thresholdId === thresholdRequestId && Number.isFinite(device.warning_threshold) && Number.isFinite(device.critical_threshold)) {
                thresholds.value = { warning: device.warning_threshold, critical: device.critical_threshold };
            }
        })
        .catch(() => {});
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
onMounted(() => { updateChartTheme(); refresh(); });
onUnmounted(() => { requestId++; thresholdRequestId++; });
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
        <section v-if="stats && stats.current_timestamp != null && !loading && !error" class="mt-6">
            <h2>Последние 10 минут измерений</h2>
            <p class="text-sm text-surface-500 mb-4">До последнего измерения выбранного дня</p>
            <p v-if="thresholds" class="text-sm mb-4">Текущие пороги: <span style="color: #b45309">предупреждение — {{ thresholds.warning }} дБ</span>, <span style="color: #dc2626">критический — {{ thresholds.critical }} дБ</span>. Они применяются к уровню после поправки на тип дома; график показывает исходные измерения. Для прошлых дат и другого времени суток пороги могли отличаться.</p>
            <p v-else class="text-sm mb-4">Текущие пороги недоступны.</p>
            <Chart v-if="chartPoints.length" type="line" :data="chartData" :options="chartOptions" class="h-80" aria-label="График уровня шума по времени" />
            <p v-else role="status">Нет измерений для построения графика.</p>
        </section>
    </div>
</template>
