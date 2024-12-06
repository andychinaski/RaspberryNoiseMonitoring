<template>
    <div class="noise-stats">
      <div v-if="noiseStats" class="current-noise-container">
        <div :style="currentNoiseStyle">
          <p class="current-noise">{{ noiseStats.current_noise }} дБ</p>
        </div>
      </div>
  
      <div v-if="noiseStats" class="stats-footer">
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
    computed: {
      currentNoiseStyle() {
        const currentNoise = this.noiseStats ? this.noiseStats.current_noise : 0;
        let color = 'green';
        if (currentNoise >= 80) {
          color = 'red';
        } else if (currentNoise >= 20) {
          const red = Math.min(255, (currentNoise - 20) * 5);
          const green = Math.max(0, 255 - red);
          color = `rgb(${red}, ${green}, 0)`; // Градиент от зеленого к красному
        }
        return {
          color: color,
          fontSize: '80px', // Очень большая цифра
          fontWeight: 'bold',
          textAlign: 'center',
        };
      },
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
  .noise-stats {
    height: 350px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
  
  .current-noise-container {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-grow: 1;
  }
  
  .error {
    color: red;
    margin-top: 10px;
  }
  </style>