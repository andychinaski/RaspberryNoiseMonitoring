import datetime
from .noise_sensor import NoiseSensor
from .logger import FileLogger

class NoiseAnalyzer:
    def __init__(self, config):
        self.config = config
        self.sensor = NoiseSensor()
        self.high_noise_start = None
        self.noise_logger = FileLogger('noiseMeasuring')

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

        # Проверка уровней шума
        if adjusted_noise_level > warning_level:
            if not self.high_noise_start:
                self.high_noise_start = datetime.datetime.now()  # Начало превышения
            elif (datetime.datetime.now() - self.high_noise_start).total_seconds() > 3:
                if adjusted_noise_level > critical_level:
                    print(f"Критический уровень шума держится более 3 секунд: {adjusted_noise_level} дБ")
                else:
                    print(f"Высокий уровень шума держится более 3 секунд: {adjusted_noise_level} дБ")
        else:
            self.high_noise_start = None  # Сброс таймера при нормализации

        self.noise_logger.log("Текущий уровень {current_noise_level}")