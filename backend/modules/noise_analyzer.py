import datetime
from .noise_sensor import NoiseSensor
from .database import insert_measurement, insert_warning_event
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

        # Вставляем измерение в базу данных
        measurement_id = insert_measurement(current_noise_level)

        # Проверка уровней шума
        if adjusted_noise_level > warning_level:
            if not self.high_noise_start:
                self.high_noise_start = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')  # Начало превышения
            elif (datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S') - self.high_noise_start).total_seconds() > self.checkDuration:
                if adjusted_noise_level > critical_level:
                    # Добавление записи о критическом уровне шума в базу данных
                    insert_warning_event(measurement_id, 'CRITICAL', f"Критический уровень шума держится более {self.checkDuration} секунд")
                    self.notifier.send_notification(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'), current_noise_level, 'CRITICAL', f"Критический уровень шума держится более {self.checkDuration} секунд")
                else:
                    # Добавление записи о высоком уровне шума в базу данных
                    insert_warning_event(measurement_id, 'WARNING', f"Высокий уровень шума держится более {self.checkDuration} секунд")
                    self.notifier.send_notification(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'), current_noise_level, 'WARNING', f"Критический уровень шума держится более {self.checkDuration} секунд")
        else:
            self.high_noise_start = None  # Сброс таймера при нормализации