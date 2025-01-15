import random
import math
import time

class NoiseSensor:
    def __init__(self, base_noise=10, variation=5, peak_chance=0.1, peak_range=(60, 100), peak_duration_range=(1, 30)):
        """
        Эмулятор датчика шума с реалистичной генерацией уровней шума.
        :param base_noise: Базовый уровень шума (дБ).
        :param variation: Максимальное отклонение от фонового шума (дБ).
        :param peak_chance: Вероятность появления шумового пика (от 0 до 1).
        :param peak_range: Диапазон уровней шумовых пиков (дБ).
        :param peak_duration_range: Диапазон длительности шумового пика (в секундах).
        """
        self.base_noise = base_noise
        self.variation = variation
        self.peak_chance = peak_chance
        self.peak_range = peak_range
        self.peak_duration_range = peak_duration_range
        self.time_step = 0
        self.peak_duration = 0  # Длительность текущего пикового шума
        self.peak_start_time = None  # Время начала пика

    def get_noise_level(self):
        """
        Генерирует реалистичный уровень шума.
        Возвращает уровень шума в дБ, который состоит из базового шума, 
        случайных колебаний и возможных шумовых пиков.
        :return: Уровень шума в дБ.
        """
        # Если пиковая фаза активна
        if self.peak_duration > 0:
            # Уменьшаем длительность пикового шума
            self.peak_duration -= 1
            return round(random.uniform(*self.peak_range))  # Генерируем шум в пределах пикового диапазона
        
        # Если пик только что закончился, начинаем следующий цикл
        if self.peak_duration == 0 and self.peak_start_time is not None:
            self.peak_start_time = None
        
        # Фоновый шум с синусоидальными колебаниями
        background_noise = self.base_noise + math.sin(self.time_step) * self.variation
        
        # Добавление случайного отклонения к фоновому шуму
        background_noise += random.uniform(-self.variation, self.variation)
        
        # Проверка на появление шумового пика
        if random.random() < self.peak_chance:
            # Начинаем новый пик
            self.peak_duration = random.randint(*self.peak_duration_range)
            self.peak_start_time = time.time()  # Засекаем время начала пика
            return round(random.uniform(*self.peak_range))  # Генерируем шум в пределах пикового диапазона
        
        # Инкремент временного шага для плавности изменений
        self.time_step += 0.1
        return round(background_noise)