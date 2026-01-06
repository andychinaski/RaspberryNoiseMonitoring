from flask import Flask, jsonify, request
from flask_cors import CORS
from .database import get_noise_stats, get_sent_notifications_by_date, get_events_by_date
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
device_start_time = time.time()

# Устаревшая версия эндпоинта для получения измерений
@app.route('/api/measurements', methods=['GET'])
def get_measurements():
    date = request.args.get('date')
    measurements = get_events_by_date(date=date, only_critical=False)

    data = [
        {
            'id': m['event_id'],
            'timestamp': m['timestamp'],
            'noise_level': m['noise_level']
        }
        for m in measurements
    ]
    return jsonify(data)

@app.route('/api/noise-stats', methods=['GET'])
def get_noise_stats_route():
    date = request.args.get('date')

    if not date:
        return jsonify({"error": "Date parameter is required"}), 400

    try:
        stats = get_noise_stats(date)

        # Если данных нет, возвращаем нули, но полную структуру
        if not stats:
            return jsonify({
                "date": date,
                "min_noise": 0,
                "max_noise": 0,
                "current_noise": 0,
                "current_timestamp": None,
                "event_type": "NORMAL",
                "notifications_sent": 0,
                "last_10_minutes": []
            })

        # Нормальный ответ — отдаём всё, что вернула функция
        response = {
            "date": date,
            "min_noise": stats["min_noise"],
            "max_noise": stats["max_noise"],
            "current_noise": stats["current_noise"],
            "current_timestamp": stats["current_timestamp"],
            "event_type": stats["event_type"],
            "notifications_sent": stats["notifications_sent"],
            "last_10_minutes": stats["last_10_minutes"]
        }

        return jsonify(response)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Устаревшая версия эндпоинта для получения критических событий
@app.route('/api/critical-events', methods=['GET'])
def get_critical_events():
    """
    Эндпоинт для получения критических событий.
    Поддерживает фильтрацию по дате (date=YYYY-MM-DD).
    """
    date = request.args.get('date')

    # Забираем только критические события
    rows = get_events_by_date(date=date, only_critical=True)

    # Преобразуем в JSON-структуру
    data = [
        {
            "id": row['event_id'],
            "timestamp": row['timestamp'],
            "noise_level": row['noise_level'],
            "event_type": row['event_type'],
            "info": row['info']
        }
        for row in rows
    ]

    return jsonify(data)

@app.route('/api/notifications', methods=['GET'])
def get_notifications():
    """
    Эндпоинт получения уведомлений.
    Поддерживает параметры:
      - date=YYYY-MM-DD (опционально)
      - only_sent=true/false/1/0 (опционально)
    """
    date = request.args.get('date')

    # Нормальное преобразование флага only_sent
    only_sent_param = request.args.get('only_sent', 'false').lower()
    only_sent = only_sent_param in ('1', 'true', 'yes', 'y')

    # Получаем данные
    rows = get_sent_notifications_by_date(
        date=date,
        only_sent=only_sent
    )

    # Приводим к единому JSON-формату
    data = [
        {
            "id": row["id"],
            "message": row["message"],
            "sent_at": row["sent_at"],
            "status": row["status"]
        }
        for row in rows
    ]

    return jsonify(data)

@app.route('/api/device-info', methods=['GET'])
def get_device_info():
    """
    Эндпоинт для получения информации об устройстве:
    device_name, uptime (секунды), measurement_frequency, warning_threshold, critical_threshold
    Пороги берутся из default.yaml в зависимости от текущего времени суток (day/night).
    """
    uptime = int(time.time() - device_start_time)
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

@app.route('/api/events', methods=['GET'])
def get_events():
    """
    Эндпоинт получения всех событий/измерений.
    Поддерживает параметры:
      - date=YYYY-MM-DD (опционально)
      - only_critical=true/false/1/0 (опционально)
    """
    date = request.args.get('date')

    # Нормальное преобразование флага критичности
    only_critical_param = request.args.get('only_critical', 'false').lower()
    only_critical = only_critical_param in ('1', 'true', 'yes', 'y')

    # Получаем данные
    rows = get_events_by_date(date=date, only_critical=only_critical)

    # Приводим к единому читаемому JSON-формату
    data = [
        {
            "id": row["event_id"],
            "timestamp": row["timestamp"],
            "noise_level": row["noise_level"],
            "type": row["event_type"],
            "info": row.get("info")  # если есть поле info — добавляем, нет — будет None
        }
        for row in rows
    ]

    return jsonify(data)

@app.route('/api/device-reboot', methods=['GET'])
def reboot_device():
    global device_start_time

    device_start_time = time.time()

    return jsonify({
        "status": "ok",
        "message": "Device rebooted"
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)