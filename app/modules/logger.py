import os
import logging
from datetime import datetime

class FileLogger:
    def __init__(self, log_type):
        self.log_type = log_type
        self.log_dir = "logs"
        self.log_path = os.path.join(self.log_dir, self.log_type)

        # Проверка наличия папки, создание если нет
        if not os.path.exists(self.log_dir):
            os.makedirs(self.log_dir)
        if not os.path.exists(self.log_path):
            os.makedirs(self.log_path)

        # Настройка логирования
        self.logger = logging.getLogger(self.log_type)
        self.logger.setLevel(logging.DEBUG)
        
        # Формирование имени файла лога с датой в формате yyyymmdd
        log_filename = f'{self.log_type}_{datetime.now().strftime("%Y%m%d")}.log'
        file_handler = logging.FileHandler(os.path.join(self.log_path, log_filename), encoding='utf-8')
        file_handler.setLevel(logging.DEBUG)
        
        # Форматирование записи лога
        formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
        file_handler.setFormatter(formatter)
        
        # Добавление обработчика в логгер
        self.logger.addHandler(file_handler)

    def log(self, message, level="INFO"):
        level_map = {
            "INFO": self.logger.info,
            "WARNING": self.logger.warning,
            "CRITICAL": self.logger.critical
        }
        level_map.get(level, self.logger.info)(message)