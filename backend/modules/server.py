from flask import Flask, jsonify, request
from flask_cors import CORS
from .database import get_measurements_by_date, get_min_max_noise

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
def get_noise_stats():
    # Получение параметра даты из запроса
    date = request.args.get('date')

    if not date:
        return jsonify({"error": "Date parameter is required"}), 400

    try:
        # Получаем минимальный и максимальный уровень шума за указанный день
        noise_stats = get_min_max_noise(date)
        
        if noise_stats:
            min_noise, max_noise = noise_stats
            return jsonify({
                "date": date,
                "min_noise": min_noise,
                "max_noise": max_noise
            })
        else:
            return jsonify({"error": f"No data found for date {date}"}), 400

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000, debug=True)