import { apiUrl } from './api.js';

export async function getNoiseStats(date) {
    const query = new URLSearchParams({ date });
    const response = await fetch(`${apiUrl('/api/noise-stats')}?${query}`);

    if (!response.ok) {
        const error = new Error(`Ошибка получения статистики: HTTP ${response.status}`);
        error.status = response.status;
        throw error;
    }

    return response.json();
}
