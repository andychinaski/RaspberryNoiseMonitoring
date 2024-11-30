import yaml
from modules.noise_analyzer import NoiseAnalyzer

# Функция для загрузки конфигурации из файлов
def load_config(default_file='app/modules/default.yaml', user_file='app/config.yaml'):
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

    # Инициализируем анализатор с конфигом
    analyzer = NoiseAnalyzer(config)

    # Пример анализа уровня шума
    analyzer.analyze_noise()
