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
import apiService from "@/helpers/api.js";

export default {
  data() {
    return {
      noiseStats: null,
      error: null,
      pollingInterval: null,
    };
  },
  computed: {
    currentNoiseStyle() {
    const currentNoise = this.noiseStats ? this.noiseStats.current_noise : 0;
    
    // Массив цветов для плавного перехода
    const colors = [
      { level: 0, color: "rgb(0, 255, 0)" },       // Зеленый
      { level: 20, color: "rgb(255, 255, 0)" },     // Желтый
      { level: 40, color: "rgb(255, 165, 0)" },     // Оранжевый
      { level: 60, color: "rgb(255, 0, 0)" },       // Красный
      { level: 80, color: "rgb(139, 0, 0)" },       // Темно-красный
    ];

    // Функция для нахождения цвета по текущему уровню
    function getGradientColor(noise) {
      let startColor = colors[0];
      let endColor = colors[1];

      for (let i = 1; i < colors.length; i++) {
        if (noise < colors[i].level) {
          endColor = colors[i];
          break;
        }
        startColor = colors[i];
      }

      // Интерполяция между начальным и конечным цветом
      const ratio = (noise - startColor.level) / (endColor.level - startColor.level);
      const startRGB = startColor.color.match(/\d+/g).map(Number);
      const endRGB = endColor.color.match(/\d+/g).map(Number);
      
      const interpolatedColor = startRGB.map((start, i) => {
        return Math.round(start + ratio * (endRGB[i] - start));
      });

      return `rgb(${interpolatedColor.join(",")})`;
    }

    const color = getGradientColor(currentNoise);

    return {
      color: color,
      fontSize: "80px", // Очень большая цифра
      fontWeight: "bold",
      textAlign: "center",
    };
  },
  },
  methods: {
    async fetchNoiseStats() {
      try {
        const currentDate = new Date().toISOString().split("T")[0];
        const data = await apiService.get("noise-stats", { date: currentDate });
        if (data.error) {
          this.error = data.error;
        } else {
          this.noiseStats = data;
          this.error = null;
        }
      } catch (err) {
        this.error = `Ошибка: ${err.message}`;
      }
    },
    startPolling() {
      this.pollingInterval = setInterval(this.fetchNoiseStats, 5000); // Интервал в 5 секунд
    },
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
        this.pollingInterval = null;
      }
    },
  },
  mounted() {
    this.fetchNoiseStats(); // Запрос статистики при монтировании компонента
    this.startPolling(); // Запускаем polling при монтировании
  },
  beforeDestroy() {
    this.stopPolling(); // Очищаем интервал при уничтожении компонента
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