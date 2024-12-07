import sqlite3
import os

DATABASE_PATH = os.path.join(os.path.dirname(__file__), 'database.db')

''' ИНИЦИАЛИЗАЦИЯ БД НАЧАЛО '''

def initialize_database():
    """Инициализация базы данных и создание таблиц, если они не существуют"""
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    # Создание таблицы для хранения измерений
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS measurements (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        noise_level INTEGER
    )
    ''')

    # Создание таблицы для хранения событий предупреждений
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS warning_events (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        measure_id INTEGER NOT NULL,
        event TEXT CHECK(event IN ('WARNING', 'CRITICAL')) NOT NULL,
        info TEXT,
        FOREIGN KEY (measure_id) REFERENCES measurements(id) ON DELETE CASCADE
    )
    ''')

    # Создание таблицы для хранения уведомлений Telegram
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

def insert_measurement(noise_level):
    """
    Добавляет запись в таблицу measurements.

    :param noise_level: Уровень шума (целое число).
    :return: ID новой добавленной записи.
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    cursor.execute('''
    INSERT INTO measurements (noise_level) 
    VALUES (?)
    ''', (noise_level,))

    conn.commit()
    new_id = cursor.lastrowid  # Получение ID новой записи
    conn.close()
    return new_id

def insert_warning_event(measure_id, event, info):
    """
    Добавляет запись в таблицу warning_events.
    
    :param measure_id: Идентификатор записи в таблице measurements
    :param event: Тип события ('WARNING' или 'CRITICAL')
    :param info: Сообщение с дополнительной информацией
    :return: Идентификатор новой записи
    """
    if event not in ('WARNING', 'CRITICAL'):
        raise ValueError("Значение event должно быть 'WARNING' или 'CRITICAL'")

    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    cursor.execute('''
    INSERT INTO warning_events (measure_id, event, info)
    VALUES (?, ?, ?)
    ''', (measure_id, event, info))

    conn.commit()
    new_id = cursor.lastrowid  # Получаем ID новой записи
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

    cursor.execute('''
    INSERT INTO telegram_notifications (message, status)
    VALUES (?, ?)
    ''', (message, status))

    conn.commit()
    new_id = cursor.lastrowid  # Получение ID новой записи
    conn.close()
    return new_id

''' ОСНОВНЫЕ ИНСЕРТЫ КОНЕЦ '''

def get_measurements_by_date(date=None):
    """
    Получение записей из таблицы measurements.
    Если дата передана, возвращаются записи только за этот день.
    Если дата не указана, возвращаются все записи.

    :param date: Дата в формате 'YYYY-MM-DD' (опционально).
    :return: Список записей.
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    if date:
        # Запрос для выборки записей за конкретную дату
        cursor.execute("""
            SELECT * FROM measurements
            WHERE DATE(timestamp) = ?
        """, (date,))
    else:
        # Запрос для выборки всех записей
        cursor.execute("SELECT * FROM measurements")

    rows = cursor.fetchall()
    conn.close()

    return rows

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

def get_critical_events_by_date(date=None):
    """
    Получение критических событий из таблицы warning_events с дополнительной информацией.
    Если дата передана, возвращаются события только за этот день.
    Если дата не указана, возвращаются все события.

    :param date: Дата в формате 'YYYY-MM-DD' (опционально).
    :return: Список событий с деталями (ID, дата, уровень шума, тип события, информация).
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    if date:
        query = """
            SELECT 
                we.id AS event_id,
                m.timestamp AS timestamp,
                m.noise_level AS noise_level,
                we.event AS event_type,
                we.info AS event_info
            FROM warning_events we
            JOIN measurements m ON we.measure_id = m.id
            WHERE we.event IN ('WARNING', 'CRITICAL') AND DATE(m.timestamp) = ?
            ORDER BY m.timestamp DESC
        """
        cursor.execute(query, (date,))
    else:
        query = """
            SELECT 
                we.id AS event_id,
                m.timestamp AS timestamp,
                m.noise_level AS noise_level,
                we.event AS event_type,
                we.info AS event_info
            FROM warning_events we
            JOIN measurements m ON we.measure_id = m.id
            WHERE we.event IN ('WARNING', 'CRITICAL')
            ORDER BY m.timestamp DESC
        """
        cursor.execute(query)

    rows = cursor.fetchall()
    conn.close()

    # Преобразуем результаты в список словарей
    events = [
        {
            "id": row[0],
            "timestamp": row[1],
            "noise_level": row[2],
            "type": row[3],
            "info": row[4],
        }
        for row in rows
    ]

    return events

def get_last_notification_date():
    """
    Получение даты последнего уведомления из таблицы telegram_notifications.
    
    :return: Дата последнего уведомления в формате 'YYYY-MM-DD', или None, если уведомлений нет.
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT MAX(timestamp) FROM telegram_notifications
    """)
    
    last_notification_date = cursor.fetchone()[0]  # Извлекаем дату из результата запроса
    conn.close()

    return last_notification_date