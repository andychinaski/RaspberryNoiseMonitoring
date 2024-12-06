<template>
    <div class="log-output">
      <div class="log-viewer">
        <ul>
          <li v-for="log in logs" :key="log.timestamp">
            <span :class="'log-info'">
              {{ log.timestamp }} - {{ log.noise_level }}
            </span>
          </li>
        </ul>
      </div>
      <div class="date-picker">
          <input type="date" id="date" v-model="selectedDate"/>
      </div>
    </div>      
</template>
  
  <script>
    const BASE_API_URL = "http://localhost:5000";   

  export default {
    props: {
      logType: {
        type: String,
        required: true,
      },
      title: {
        type: String,
        default: "Logs",
      },
    },
    data() {
      return {
        logs: [],
        selectedDate: new Date().toISOString().substr(0, 10), // Сегодняшняя дата
      };
    },
    methods: {
      fetchLogs() {
        const apiUrl = `${BASE_API_URL}/api/measurements`;
        fetch(apiUrl)
          .then((response) => response.json())
          .then((data) => {
            this.logs = data;
          })
          .catch((error) => {
            console.error(`Error fetching logs for type ${this.logType}:`, error);
          });
      },
    },
    mounted() {
      this.fetchLogs();
    },
  };
  </script>
  
  <style scoped>
  .log-output{
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
  
  .log-warning {
    color: #ff9800; /* Оранжевый */
  }
  
  .log-critical {
    color: #f44336; /* Красный */
  }
  </style>