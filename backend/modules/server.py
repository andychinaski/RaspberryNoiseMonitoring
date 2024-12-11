from flask import Flask, jsonify, request
from flask_cors import CORS
from .database import get_measurements_by_date, get_noise_stats, get_critical_events_by_date

app = Flask(__name__)
CORS(app)  # Разрешаем CORS-запросы

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

if __name__ == '__main__':
    app.run(port=5000, debug=True)