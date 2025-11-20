import sqlite3
import os
import datetime

DATABASE_PATH = os.path.join(os.path.dirname(__file__), 'database.db')

''' ИНИЦИАЛИЗАЦИЯ БД НАЧАЛО '''

def initialize_database():
    """Инициализация базы данных и создание таблиц, если они не существуют"""
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    cursor.execute('''
    CREATE TABLE IF NOT EXISTS measurements (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        noise_level INTEGER,
        event TEXT,
        info TEXT
    )
    ''')

    cursor.execute('''
    CREATE TABLE IF NOT EXISTS telegram_notifications (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        message TEXT NOT NULL,
        sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        status TEXT CHECK(status IN ('sent', 'failed')) DEFAULT 'sent'
    )
    ''')

    conn.commit()
    conn.close()
    print("База данных инициализирована.")

''' ИНИЦИАЛИЗАЦИЯ БД КОНЕЦ '''

''' ОСНОВНЫЕ ИНСЕРТЫ НАЧАЛО '''

def insert_measurement(noise_level, event=None, info=None):
    """
    Добавляет запись в таблицу measurements.
    
    :param noise_level: уровень шума
    :param event: тип события ('WARNING', 'CRITICAL' или любое другое значение)
    :param info: текст с описанием события
    :return: ID новой записи
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    current_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    cursor.execute('''
        INSERT INTO measurements (timestamp, noise_level, event, info)
        VALUES (?, ?, ?, ?)
    ''', (current_time, noise_level, event, info))

    conn.commit()
    new_id = cursor.lastrowid
    conn.close()

    return new_id

def insert_telegram_notification(message, status="sent"):
    """
    Добавляет запись в таблицу telegram_notifications.

    :param message: Сообщение, отправленное в Telegram (строка).
    :param status: Статус отправки (по умолчанию "sent"). Допустимые значения: "sent", "failed".
    :return: ID новой добавленной записи.
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    current_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    cursor.execute('''
    INSERT INTO telegram_notifications (message, sent_at, status)
    VALUES (?, ?, ?)
    ''', (message, current_time, status))

    conn.commit()
    new_id = cursor.lastrowid  # Получение ID новой записи
    conn.close()
    return new_id

''' ОСНОВНЫЕ ИНСЕРТЫ КОНЕЦ '''

def get_noise_stats(date):
    """
    Получение статистики уровня шума за определённый день.

    :param date: Дата в формате 'YYYY-MM-DD'
    :return: Словарь с ключами 'min_noise', 'max_noise' и 'current_noise' или None, если данных нет
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    # Получение минимального и максимального уровня шума за день
    cursor.execute("""
        SELECT MIN(noise_level), MAX(noise_level)
        FROM measurements
        WHERE DATE(timestamp) = ?
    """, (date,))
    min_max_result = cursor.fetchone()

    # Получение последней записи для текущего дня
    cursor.execute("""
        SELECT noise_level
        FROM measurements
        WHERE DATE(timestamp) = ?
        ORDER BY timestamp DESC
        LIMIT 1
    """, (date,))
    current_result = cursor.fetchone()

    conn.close()

    if min_max_result and current_result:
        min_noise, max_noise = min_max_result
        current_noise = current_result[0]
        return {
            'min_noise': min_noise,
            'max_noise': max_noise,
            'current_noise': current_noise
        }

    return None

def get_last_notification_date():
    """
    Получение времени последнего успешно отправленного уведомления из таблицы telegram_notifications.
    
    :return: Максимальная дата отправки (sent_at) в формате 'YYYY-MM-DD HH:MM:SS' или None, если уведомлений нет.
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT MAX(sent_at) 
        FROM telegram_notifications 
        WHERE status = 'sent'
    """)
    
    last_sent_at = cursor.fetchone()[0]  # Извлекаем максимальную дату
    conn.close()

    return last_sent_at

def get_sent_notifications_by_date(date=None):
    """
    Получение отправленных уведомлений из таблицы telegram_notifications.
    Если дата передана, возвращаются уведомления только за этот день.
    Если дата не указана, возвращаются все уведомления.

    :param date: Дата в формате 'YYYY-MM-DD' (опционально).
    :return: Список уведомлений с деталями (ID, сообщение, дата отправки, статус).
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    if date:
        query = """
            SELECT 
                id,
                message,
                sent_at,
                status
            FROM telegram_notifications
            WHERE DATE(sent_at) = ?
            ORDER BY sent_at DESC
        """
        cursor.execute(query, (date,))
    else:
        query = """
            SELECT 
                id,
                message,
                sent_at,
                status
            FROM telegram_notifications
            ORDER BY sent_at DESC
        """
        cursor.execute(query)

    rows = cursor.fetchall()
    conn.close()

    # Преобразуем результаты в список словарей
    notifications = [
        {
            "id": row[0],
            "message": row[1],
            "sent_at": row[2],
            "status": row[3],
        }
        for row in rows
    ]

    return notifications

def get_events_by_date(date=None, only_critical=False):
    """
    Получение измерений и событий.
    - date: 'YYYY-MM-DD' или None
    - only_critical: если True — только WARNING/CRITICAL
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    query = """
        SELECT
            id AS event_id,
            timestamp,
            noise_level,
            COALESCE(event, 'NORMAL') AS event_type,
            info
        FROM measurements
        WHERE 1 = 1
    """
    params = []

    # фильтр по дате
    if date:
        query += " AND DATE(timestamp) = ?"
        params.append(date)

    # фильтр по критичности
    if only_critical:
        query += " AND event IN ('WARNING', 'CRITICAL')"

    query += " ORDER BY timestamp DESC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    # преобразование в список словарей
    return [
        {
            "event_id": row[0],
            "timestamp": row[1],
            "noise_level": row[2],
            "event_type": row[3],
            "info": row[4]
        }
        for row in rows
    ]