export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000';

export function apiUrl(path) {
    return `${apiBaseUrl.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`;
}
