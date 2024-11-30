from modules.noise_sensor import NoiseSensor
import time

def main():
    """
    Основная функция для генерации уровня шума с помощью эмулятора.
    Уровень шума будет измеряться каждую секунду.
    """
    # Создаем экземпляр эмулятора датчика шума
    sensor = NoiseSensor(base_noise=30, variation=5, peak_chance=0.2, peak_range=(60, 100), peak_duration_range=(3, 15))
    
    # Генерируем 30 значений уровня шума каждую секунду
    for _ in range(30):
        noise_level = sensor.get_noise_level()
        print(f"Уровень шума: {noise_level} дБ")
        time.sleep(1)  # Измерения каждую секунду

if __name__ == "__main__":
    main()