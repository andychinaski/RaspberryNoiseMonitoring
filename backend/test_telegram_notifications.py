import datetime
import io
import os
import unittest
from contextlib import redirect_stdout
from unittest.mock import AsyncMock, patch

from modules.noise_analyzer import NoiseAnalyzer
from modules.tgbot import TelegramNotifier


class TelegramNotificationTests(unittest.TestCase):
    def test_incomplete_configuration_disables_notifications(self):
        message = 'Конфиг для отправки сообщений в ТГ не указан, уведомления не будут отправляться\n'
        for key in ('TELEGRAM_TOKEN', 'TELEGRAM_CHAT_ID'):
            for value in (None, '', '   '):
                with self.subTest(key=key, value=value):
                    settings = {'TELEGRAM_TOKEN': 'token', 'TELEGRAM_CHAT_ID': '123'}
                    if value is None:
                        del settings[key]
                    else:
                        settings[key] = value
                    output = io.StringIO()
                    with patch.dict(os.environ, settings, clear=True), \
                         patch('modules.tgbot.load_dotenv'), \
                         patch('modules.tgbot.Bot') as bot, redirect_stdout(output):
                        notifier = TelegramNotifier.from_environment()
                    self.assertIsNone(notifier)
                    bot.assert_not_called()
                    self.assertEqual(output.getvalue(), message)

    def test_disabled_notifications_still_record_measurements(self):
        config = {'house_type': 'test', 'delta': {'test': 0},
                  'noise_thresholds': {'day_warning': 40, 'day_critical': 55}}
        with patch('modules.noise_analyzer.TelegramNotifier.from_environment', return_value=None), \
             patch('modules.noise_analyzer.insert_measurement') as insert, \
             patch('modules.tgbot.insert_telegram_notification') as notification:
            analyzer = NoiseAnalyzer(config)
            analyzer.get_time_of_day = lambda: 'day'
            analyzer.get_current_noise_level = lambda: 80
            analyzer.high_noise_start = datetime.datetime.now() - datetime.timedelta(seconds=20)
            analyzer.analyze_noise()
        insert.assert_called_once()
        self.assertEqual(insert.call_args.kwargs['event'], 'CRITICAL')
        self.assertIsNotNone(insert.call_args.kwargs['info'])
        notification.assert_not_called()

    def test_configured_notifier_sends_and_records(self):
        settings = {'TELEGRAM_TOKEN': 'token', 'TELEGRAM_CHAT_ID': '123'}
        with patch.dict(os.environ, settings, clear=True), \
             patch('modules.tgbot.load_dotenv'), \
             patch('modules.tgbot.Bot') as bot, \
             patch('modules.tgbot.get_last_notification_date', return_value=None), \
             patch('modules.tgbot.insert_telegram_notification') as insert:
            bot.return_value.send_message = AsyncMock()
            notifier = TelegramNotifier.from_environment()
            notifier.send_notification('2026-01-01 12:00:00', 80, 'CRITICAL', 'noise')
        bot.assert_called_once_with(token='token')
        bot.return_value.send_message.assert_awaited_once()
        self.assertEqual(bot.return_value.send_message.await_args.kwargs['chat_id'], '123')
        self.assertEqual(insert.call_args.args[1], 'sent')


if __name__ == '__main__':
    unittest.main()
