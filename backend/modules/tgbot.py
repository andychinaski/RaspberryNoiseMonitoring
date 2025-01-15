import asyncio
import os
from datetime import datetime, timedelta
from telegram import Bot
from dotenv import load_dotenv
from .database import get_last_notification_date, insert_telegram_notification

class TelegramNotifier:
    def __init__(self, db_path):
        # Загружаем переменные из .env
        load_dotenv(os.path.join(os.path.dirname(__file__), '../.env'))
        self.token = os.getenv("TELEGRAM_TOKEN")
        self.chat_id = os.getenv("TELEGRAM_CHAT_ID")
        self.db_path = db_path
        self.bot = Bot(token=self.token)

    def send_notification(self, timestamp, noise_level, event_type, info):
        """
        Отправляет уведомление через Telegram, если за последние 5 минут не было отправлено уведомлений.
        Если уведомление отправлено успешно, сохраняет запись в базе данных.
        """
        if not self._should_send_notification():
            return

        # Формируем сообщение
        message = (
            f"*Критическое событие*\n"
            f"Время: {timestamp}\n"
            f"Уровень шума: {noise_level} дБ\n"
            f"Тип: {event_type}\n"
            f"Информация: {info}"
        )

        try:
            # Отправляем уведомление
            asyncio.run(self._async_send_notification(message))

            # Сохраняем запись об отправленном уведомлении
            insert_telegram_notification(message, "sent")

        except Exception as e:        
            # Сохраняем запись о неудачной отправке уведомления
            insert_telegram_notification(message, "failed")

    async def _async_send_notification(self, message):
        """
        Асинхронный метод отправки уведомлений.
        """
        await self.bot.send_message(chat_id=self.chat_id, text=message, parse_mode="Markdown")

    def _should_send_notification(self):
        """
        Проверяет, прошло ли 5 минут с момента последнего уведомления.
        :return: True, если уведомление можно отправить, иначе False.
        """
        last_notification_time = get_last_notification_date()
        
        if last_notification_time:
            last_notification_time = datetime.fromisoformat(last_notification_time)
            if datetime.now() - last_notification_time < timedelta(minutes=5):
                return False

        return True