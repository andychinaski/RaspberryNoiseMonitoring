import os
import logging

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
        file_handler = logging.FileHandler(os.path.join(self.log_path, f'{self.log_type}.log'))
        file_handler.setLevel(logging.DEBUG)
        formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
        file_handler.setFormatter(formatter)
        self.logger.addHandler(file_handler)

    def log(self, message):
        self.logger.info(message)