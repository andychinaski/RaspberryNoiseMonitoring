<template>
  <div class="log-output">
    <div class="log-viewer">
      <ul>
        <li v-for="log in logs" :key="log.timestamp">
          <template v-if="apiEndpoint === 'measurements'">
            <span class="log-info">
              {{ new Date(log.timestamp).toLocaleString().replace(',', '') }}  - Уровень шума: {{ log.noise_level }} дБ
            </span>
          </template>
          <template v-else-if="apiEndpoint === 'critical-events'">
            <span class="log-info">
              {{ new Date(log.timestamp).toLocaleString().replace(',', '') }} - Событие: {{ log.event_type }} ({{ log.info }})
            </span>
          </template>
          <template v-else-if="apiEndpoint === 'notifications'">
            <span class="log-info">
              {{ new Date(log.sent_at).toLocaleString().replace(',', '') }} - Отправлено сообщение: {{ log.message}}
            </span>
          </template>
          <v-divider/>
        </li>
      </ul>
    </div>
    <div class="date-picker">
      <input type="date" id="date" v-model="selectedDate" @change="fetchLogs" />
    </div>
  </div>
</template>

<script>
import apiService from "@/helpers/api.js";

export default {
  props: {
    apiEndpoint: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      logs: [],
      selectedDate: new Date().toISOString().split("T")[0], // Сегодняшняя дата
      pollingInterval: null,
    };
  },
  methods: {
    async fetchLogs() {
      try {
        const params = { date: this.selectedDate };
        const data = await apiService.get(this.apiEndpoint, params);
        this.logs = data;
      } catch (error) {
        console.error(`Error fetching logs from ${this.apiEndpoint}:`, error);
      }
    },
    startPolling() {
      this.pollingInterval = setInterval(this.fetchLogs, 5000); // Интервал в 5 секунд
    },
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
        this.pollingInterval = null;
      }
    },
  },
  mounted() {
    this.fetchLogs();
    this.startPolling(); // Запускаем polling при монтировании
  },
  beforeDestroy() {
    this.stopPolling(); // Очищаем интервал при уничтожении компонента
  },
};
</script>

<style scoped>
.log-output {
  height: 350px;
}

.log-viewer {
  height: 300px;
  overflow-y: auto;
  background-color: #f5f5f5; /* Светло-серый фон */
  padding: 16px;
  border: 1px solid #ddd; /* Тонкая граница */
  border-radius: 4px; /* Скругленные углы */
}

.date-picker {
  margin-top: 10px;
}

ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
}

li {
  margin: 4px 0;
}

.log-info {
  color: #202020;
}
</style>