from flask import Flask, jsonify, request
import os
from datetime import datetime

app = Flask(__name__)

# Указываем базовый путь до логов
LOGS_BASE_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), '../app/logs'))

# Функция для чтения логов
def read_logs(log_type="noiseMeasuring"):
    log_dir = os.path.join(LOGS_BASE_PATH, log_type)
    log_file = os.path.join(log_dir, f'{log_type}_{datetime.now().strftime("%Y%m%d")}.log')

    if not os.path.exists(log_file):
        return []

    logs = []
    with open(log_file, 'r', encoding='utf-8') as f:
        for line in f:
            timestamp, level, message = line.strip().split(' - ', 2)
            logs.append({
                'timestamp': timestamp,
                'level': level,
                'message': message
            })
    return logs

@app.route('/api/logs', methods=['GET'])
def get_logs():
    log_type = request.args.get('type', 'noiseMeasuring')  # Получаем тип лога через query параметр
    logs = read_logs(log_type)
    return jsonify(logs)

if __name__ == '__main__':
    app.run(debug=True)