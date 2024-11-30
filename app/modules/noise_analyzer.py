import datetime
from .noise_sensor import NoiseSensor

class NoiseAnalyzer:
    def __init__(self, config):
        self.config = config
        self.sensor = NoiseSensor()

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
        day_warning = thresholds[f'{current_time_of_day}_warning']
        day_critical = thresholds[f'{current_time_of_day}_critical']

        if adjusted_noise_level > day_critical:
            print(f"Критический уровень шума! {adjusted_noise_level} дБ")
        elif adjusted_noise_level > day_warning:
            print(f"Предупреждение! Уровень шума высокий: {adjusted_noise_level} дБ")
        else:
            print(f"Шум в норме: {adjusted_noise_level} дБ")