<template>
      <div class="log-viewer">
        <ul>
          <li v-for="log in logs" :key="log.timestamp">
            <span :class="'log-info'">
              {{ log.timestamp }} - {{ log.noise_level }}
            </span>
          </li>
        </ul>
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
  .log-viewer {
    height: 300px;
    overflow-y: auto;
    background-color: #f5f5f5; /* Светло-серый фон */
    padding: 16px;
    border: 1px solid #ddd; /* Тонкая граница */
    border-radius: 4px; /* Скругленные углы */
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