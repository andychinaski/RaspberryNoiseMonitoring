from flask import Flask
import yaml
import time
from modules.noise_analyzer import NoiseAnalyzer
from modules.database import initialize_database
from modules.tgbot import TelegramNotifier
from modules.server import app

# Функция для загрузки конфигурации из файлов
def load_config(default_file='default.yaml', user_file='config.yaml'):
    with open(default_file, 'r') as f:
        default_config = yaml.safe_load(f)
    with open(user_file, 'r') as f:
        user_config = yaml.safe_load(f)

    # Объединение дефолтного и пользовательского конфига
    default_config.update(user_config)
    return default_config

if __name__ == '__main__':
    # Загружаем конфиг один раз
    config = load_config()
    # Инициализируем БД
    initialize_database()
    # Инициализируем анализатор с конфигом
    analyzer = NoiseAnalyzer(config)

    # Запуск сервера Flask в фоновом режиме
    from threading import Thread
    server_thread = Thread(target=app.run, kwargs={'host': '0.0.0.0', 'port': 5000, 'debug': False})
    server_thread.start()

    # Запуск мониторинга на 2 минуты
    start_time = time.time()
    while time.time() - start_time < 1200:
        analyzer.analyze_noise()  # Анализируем уровень шума
        time.sleep(5)  # Задержка в 5 секунд между анализами