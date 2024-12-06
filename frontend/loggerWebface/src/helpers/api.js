class ApiService {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async get(endpoint, params = {}) {
        const url = new URL(`${this.baseUrl}/${endpoint}`);
        Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));

        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`Ошибка ${response.status}: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('GET запрос ошибка:', error);
            throw error;
        }
    }

    async post(endpoint, data = {}) {
        try {
            const response = await fetch(`${this.baseUrl}/${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });
            if (!response.ok) {
                throw new Error(`Ошибка ${response.status}: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('POST запрос ошибка:', error);
            throw error;
        }
    }
}

// Экспорт экземпляра класса с базовым URL
const apiService = new ApiService('http://localhost:5000/api');
export default apiService;