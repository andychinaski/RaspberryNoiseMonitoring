export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000';

export function apiUrl(path) {
    return `${apiBaseUrl.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`;
}

async function getJson(path) {
    const response = await fetch(apiUrl(path));
    if (!response.ok) {
        const error = new Error(`Ошибка запроса API: HTTP ${response.status}`);
        error.status = response.status;
        throw error;
    }
    return response.json();
}

export function getNoiseStats(date) {
    const query = new URLSearchParams({ date });
    return getJson(`/api/noise-stats?${query}`);
}

export function getDeviceInfo() {
    return getJson('/api/device-info');
}

function optionalQuery(parameters) {
    const query = new URLSearchParams();
    for (const [key, value] of Object.entries(parameters)) {
        if (value !== undefined && value !== null) query.set(key, String(value));
    }
    const encoded = query.toString();
    return encoded ? `?${encoded}` : '';
}

export function getEvents(date, onlyCritical) {
    return getJson(`/api/events${optionalQuery({ date, only_critical: onlyCritical })}`);
}

export function getNotifications(date, onlySent) {
    return getJson(`/api/notifications${optionalQuery({ date, only_sent: onlySent })}`);
}

export function resetDemoUptime() {
    return getJson('/api/device-reboot');
}
