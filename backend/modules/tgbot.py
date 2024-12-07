import os
from telegram import Bot
from dotenv import load_dotenv


class TelegramNotifier:
    def __init__(self, db_path):
        # Загружаем переменные из .env
        load_dotenv(os.path.join(os.path.dirname(__file__), '../.env'))
        self.token = os.getenv("TELEGRAM_TOKEN")
        self.chat_id = os.getenv("TELEGRAM_CHAT_ID")
        self.db_path = db_path
        self.bot = Bot(token=self.token)

    def send_notification(self, message):
        """
        Отправляет уведомление через Telegram.
        """
        self.bot.send_message(chat_id=self.chat_id, text=message, parse_mode="Markdown")

    def check_env(self):
        print(f'token={self.token} chat_id={self.chat_id}')