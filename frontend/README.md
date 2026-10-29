Sakai is an application template for Vue based on the [create-vue](https://github.com/vuejs/create-vue), the recommended way to start a Vite-powered Vue projects.

Visit the [documentation](https://sakai.primevue.org/documentation) to get started.

## Окружение проекта

Основа: Sakai Vue 5.0.0. Установка зависимостей проверена с Node.js 22.11.0 и npm 10.9.0.

Из каталога `frontend` выполните `npm ci` для установки зависимостей и `npm run dev` для запуска приложения. Откройте адрес, указанный в терминале. Для сборки используйте `npm run build`, для локального просмотра результата — `npm run preview`.

## Адрес API

По умолчанию используется `http://localhost:5000`. Чтобы указать другой адрес сервера, создайте в каталоге `frontend` файл `.env.local`:

```dotenv
VITE_API_BASE_URL=http://localhost:5000
```

Указывайте адрес сервера без завершающего `/api`. Перезапустите `npm run dev` после изменения файла; для готовой сборки необходимо выполнить сборку заново. При открытии приложения с другого устройства `localhost` обозначает это устройство, поэтому укажите доступный ему адрес сервера, например адрес компьютера в локальной сети. Значение попадает в код приложения и доступно в браузере: не помещайте в него секреты. Файл `.env.local` не отслеживается Git.

Общий модуль `src/service/api.js` предоставляет `apiBaseUrl` и `apiUrl('/api/device-info')` для будущих запросов. Сам модуль запросов не отправляет; разделы пока находятся в разработке.
