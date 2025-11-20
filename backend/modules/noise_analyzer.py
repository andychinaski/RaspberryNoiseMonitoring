import datetime
from .noise_sensor import NoiseSensor
from .database import insert_measurement
from .tgbot import TelegramNotifier

class NoiseAnalyzer:
    def __init__(self, config):
        self.config = config
        self.sensor = NoiseSensor()
        self.notifier = TelegramNotifier()
        self.high_noise_start = None
        self.checkDuration = 10

    def get_current_noise_level(self):
        return self.sensor.get_noise_level()

    def get_time_of_day(self):
        current_time = datetime.datetime.now()
        if current_time.weekday() < 5:
            if 7 <= current_time.hour < 22:
                return 'day'
            else:
                return 'night'
        else:
            if 9 <= current_time.hour < 22:
                return 'day'
            else:
                return 'night'

    def analyze_noise(self):
        current_time_of_day = self.get_time_of_day()
        current_noise_level = self.get_current_noise_level()

        apartment_type = self.config['house_type']
        delta = self.config['delta'][apartment_type]
        adjusted_noise_level = current_noise_level - delta

        thresholds = self.config['noise_thresholds']
        warning_level = thresholds[f'{current_time_of_day}_warning']
        critical_level = thresholds[f'{current_time_of_day}_critical']

        # Базовые значения события
        event_type = 'NORMAL'
        info = None

        now = datetime.datetime.now()

        # --- Определяем тип события ---
        if adjusted_noise_level > critical_level:
            event_type = 'CRITICAL'
        elif adjusted_noise_level > warning_level:
            event_type = 'WARNING'
        else:
            # Шум нормализовался, сбрасываем таймер
            self.high_noise_start = None

        # --- Логика превышения во времени ---
        if event_type in ('WARNING', 'CRITICAL'):
            if not self.high_noise_start:
                # начало превышения
                self.high_noise_start = now
            else:
                duration = (now - self.high_noise_start).total_seconds()
                if duration > self.checkDuration:
                    # превышение длительное — формируем сообщение
                    if event_type == 'CRITICAL':
                        info = f"Критический уровень шума держится более {int(duration)} секунд"
                    else:
                        info = f"Высокий уровень шума держится более {int(duration)} секунд"

                    # Отправка уведомления
                    self.notifier.send_notification(
                        now.strftime('%Y-%m-%d %H:%M:%S'),
                        current_noise_level,
                        event_type,
                        info
                    )

        # --- Вставка измерения в БД: одно место, единый формат ---
        measurement_id = insert_measurement(
            noise_level=current_noise_level,
            event=event_type,
            info=info
        )