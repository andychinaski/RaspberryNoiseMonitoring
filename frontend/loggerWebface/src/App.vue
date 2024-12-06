<template>
  <v-app>
    <!-- Шапка -->
    <v-app-bar color="primary">
      <v-toolbar-title>Noise Level Monitoring</v-toolbar-title>
    </v-app-bar>

    <!-- Основной контент -->
    <v-main>
      <v-container>
      <v-row>
        <!-- Левый верхний: Статистика за сутки -->
        <v-col cols="3">
          <v-card class="card-height">
            <v-card-title>Статистика за сутки</v-card-title>
            <v-card-text>
              <CurrentStats/>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Правый верхний: График -->
        <v-col cols="9">
          <v-card class="card-height">
            <v-card-title>Уровень шума за сутки</v-card-title>
            <v-card-text>
              <LogViewer apiEndpoint="measurements"/>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <!-- Левый нижний: Логи превышения -->
        <v-col cols="6">
          <v-card class="card-height">
            <v-card-title>Логи превышения уровня шума</v-card-title>
            <v-card-text>
              <LogViewer apiEndpoint="noiseWarnings"/>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Правый нижний: Логи уведомлений -->
        <v-col cols="6">
          <v-card class="card-height">
            <v-card-title>Логи уведомлений</v-card-title>
            <v-card-text>
              <LogViewer apiEndpoint="notifications"/>
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
import CurrentStats from "@/components/CurrentStats.vue";

export default {
  components: {
    LogViewer,
    CurrentStats,
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