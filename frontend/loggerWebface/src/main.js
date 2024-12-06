import { createApp } from 'vue';
import App from './App.vue';
import { createVuetify } from 'vuetify';
import 'vuetify/styles'; // Импорт базовых стилей
import '@mdi/font/css/materialdesignicons.css'; // Иконки Material Design
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

const vuetify = createVuetify({
    components,
    directives,
  })

const app = createApp(App);
app.use(vuetify);
app.mount('#app');