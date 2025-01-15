<template>
  <div class="log-output">
    <div class="log-viewer">
      <ul>
        <li v-for="log in logs" :key="log.timestamp">
          <template v-if="apiEndpoint === 'measurements'">
            <span class="log-info">
              {{ log.timestamp }} - Уровень шума: {{ log.noise_level }} дБ
            </span>
          </template>
          <template v-else-if="apiEndpoint === 'critical-events'">
            <span class="log-info">
              {{ log.timestamp }} - Событие: {{ log.type }} ({{ log.info }})
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
  },
  mounted() {
    this.fetchLogs();
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
  color: #0000ff; /* Синий */
}
</style>