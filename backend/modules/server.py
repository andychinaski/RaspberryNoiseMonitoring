from flask import Flask, jsonify, request
from flask_cors import CORS
from .database import get_measurements_by_date, get_noise_stats, get_critical_events_by_date, get_sent_notifications_by_date
import time
import datetime
from pathlib import Path

# Попытка загрузить конфиг (default.yaml) из папки backend
try:
    import yaml
    _config_path = Path(__file__).resolve().parents[1] / 'default.yaml'
    if _config_path.exists():
        with open(_config_path, 'r', encoding='utf-8') as f:
            CONFIG = yaml.safe_load(f)
    else:
        CONFIG = {}
except Exception:
    CONFIG = {}

app = Flask(__name__)
CORS(app)  # Разрешаем CORS-запросы

def _get_time_of_day():
    now = datetime.datetime.now()
    if now.weekday() < 5:
        return 'day' if 7 <= now.hour < 22 else 'night'
    else:
        return 'day' if 9 <= now.hour < 22 else 'night'

# Время старта приложения для расчёта uptime
_app_start_time = time.time()

@app.route('/api/measurements', methods=['GET'])
def get_measurements():
    # Получаем параметр date из запроса
    date = request.args.get('date')  
    
    # Если дата передана, фильтруем записи по дате
    measurements = get_measurements_by_date(date) if date else get_measurements_by_date()
    
    # Преобразуем результат в список словарей для JSON ответа
    data = [
        {
            'id': measurement[0],
            'timestamp': measurement[1],
            'noise_level': measurement[2]
        }
        for measurement in measurements
    ]
    return jsonify(data)

@app.route('/api/noise-stats', methods=['GET'])
def get_noise_stats_route():
    # Получение параметра даты из запроса
    date = request.args.get('date')

    if not date:
        return jsonify({"error": "Date parameter is required"}), 400

    try:
        # Получаем статистику шума за указанный день
        noise_stats = get_noise_stats(date)
        
        if noise_stats:
            return jsonify({
                "date": date,
                "min_noise": noise_stats['min_noise'],
                "max_noise": noise_stats['max_noise'],
                "current_noise": noise_stats['current_noise']
            })
        else:
            return jsonify({
                "date": date,
                "min_noise": 0,
                "max_noise": 0,
                "current_noise": 0
            })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/critical-events', methods=['GET'])
def get_critical_events():
    """
    Эндпоинт для получения критических событий.
    Поддерживает фильтрацию по дате, если передан параметр date.
    """
    # Получаем параметр date из запроса
    date = request.args.get('date')

    # Получаем критические события из базы данных
    events = get_critical_events_by_date(date) if date else get_critical_events_by_date()

    # Преобразуем результат в JSON-ответ
    return jsonify(events)

@app.route('/api/notifications', methods=['GET'])
def get_notifications():
    """
    Эндпоинт для получения отправленных уведомлений.
    Поддерживает фильтрацию по дате, если передан параметр date.
    """
    # Получаем параметр date из запроса
    date = request.args.get('date')

    # Получаем уведомления из базы данных
    notifications = get_sent_notifications_by_date(date) if date else get_sent_notifications_by_date()

    # Преобразуем результат в JSON-ответ
    return jsonify(notifications)

@app.route('/api/device-info', methods=['GET'])
def get_device_info():
    """
    Эндпоинт для получения информации об устройстве:
    device_name, uptime (секунды), measurement_frequency, warning_threshold, critical_threshold
    Пороги берутся из default.yaml в зависимости от текущего времени суток (day/night).
    """
    uptime = int(time.time() - _app_start_time)
    measurement_frequency = CONFIG.get('measurement_frequency', 5)
    try:
        measurement_frequency = int(measurement_frequency)
    except Exception:
        # оставляем значение как есть, если не удаётся привести к int
        pass

    time_of_day = _get_time_of_day()
    thresholds = CONFIG.get('noise_thresholds', {})
    warning = thresholds.get(f'{time_of_day}_warning')
    critical = thresholds.get(f'{time_of_day}_critical')

    return jsonify({
        "device_name": "Raspberry PI Emulator",
        "uptime": uptime,
        "measurement_frequency": measurement_frequency,
        "warning_threshold": warning,
        "critical_threshold": critical
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)