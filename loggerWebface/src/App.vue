<template>
  <v-app>
    <!-- Шапка -->
    <v-app-bar color="blue darken-4" dense dark>
      <v-toolbar-title>Noise Level Monitoring</v-toolbar-title>
    </v-app-bar>

    <!-- Основной контент -->
    <v-main>
      <v-container>
      <v-row>
        <!-- Левый верхний: Статистика за сутки -->
        <v-col cols="3">
          <v-card>
            <v-card-title>Статистика за сутки</v-card-title>
            <v-card-text>
              <p>Текущий уровень шума: {{ stats.currentNoise }} дБ</p>
              <p>Максимальный уровень шума: {{ stats.maxNoise }} дБ</p>
              <p>Минимальный уровень шума: {{ stats.minNoise }} дБ</p>                
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Правый верхний: График -->
        <v-col cols="9">
          <v-card>
            <v-card-title>Уровень шума за сутки</v-card-title>
            <v-card-text>
              <LogViewer logType="noiseMeasuring" title="Noise Logs" />
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <!-- Левый нижний: Логи превышения -->
        <v-col cols="6">
          <v-card>
            <v-card-title>Логи превышения уровня шума</v-card-title>
            <v-card-text>
              <ul>
                <li v-for="log in exceedLogs" :key="log.id">
                  {{ log.time }} - {{ log.message }}
                </li>
              </ul>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Правый нижний: Логи уведомлений -->
        <v-col cols="6">
          <v-card>
            <v-card-title>Логи уведомлений</v-card-title>
            <v-card-text>
              <ul>
                <li v-for="log in notificationLogs" :key="log.id">
                  {{ log.time }} - {{ log.message }}
                </li>
              </ul>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
    </v-main>
    
  </v-app>
</template>

<script>
import LogViewer from "@/components/LogViewer.vue";

export default {
  components: {
    LogViewer,
  },
  data() {
    return {
      stats: {
        maxNoise: 0,
        minNoise: 0,
        currentNoise: 0,
      },
      exceedLogs: [],
      notificationLogs: [],
    };
  },
  mounted() {
    // Логика загрузки данных будет здесь.
  },
};
</script>
