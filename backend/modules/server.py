from flask import Flask, jsonify, request
from flask_cors import CORS
from .database import get_all_measurements

app = Flask(__name__)
CORS(app)  # Разрешаем CORS-запросы

@app.route('/api/measurements', methods=['GET'])
def get_measurements():
    measurements = get_all_measurements()  # Получаем все записи из таблицы measurements
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

if __name__ == '__main__':
    app.run(port=5000, debug=True)