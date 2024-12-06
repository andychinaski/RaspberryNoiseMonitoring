<template>
    <div class="noise-stats">
      <div v-if="noiseStats">
        <p>Дата: {{ noiseStats.date }}</p>
        <p>Минимальный уровень шума: {{ noiseStats.min_noise }} дБ</p>
        <p>Максимальный уровень шума: {{ noiseStats.max_noise }} дБ</p>
      </div>
  
      <div v-if="error" class="error">
        <p>{{ error }}</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        noiseStats: null,
        error: null,
      };
    },
    methods: {
      fetchNoiseStats() {
        const currentDate = new Date().toISOString().split('T')[0]; // Текущая дата в формате YYYY-MM-DD
        const apiUrl = `http://localhost:5000/api/noise-stats?date=${currentDate}`;
        
        fetch(apiUrl)
          .then((response) => response.json())
          .then((data) => {
            if (data.error) {
              this.error = data.error;
            } else {
              this.noiseStats = data;
              this.error = null;
            }
          })
          .catch((error) => {
            this.error = `Ошибка: ${error.message}`;
          });
      },
    },
    mounted() {
      this.fetchNoiseStats(); // Запрос статистики при монтировании компонента
    },
  };
  </script>
  
  <style scoped>
  
  .error {
    color: red;
    margin-top: 10px;
  }
  </style>