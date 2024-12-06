import sqlite3
import os

DATABASE_PATH = os.path.join(os.path.dirname(__file__), 'database.db')

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

    conn.commit()
    conn.close()
    print("База данных инициализирована.")

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

def get_min_max_noise(date):
    """
    Получение минимального и максимального значения уровня шума за определённый день.

    :param date: Дата в формате 'YYYY-MM-DD'
    :return: Кортеж (min_noise, max_noise) или None, если данных нет
    """
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()

    # Выполнение запроса для получения минимального и максимального значения шума
    cursor.execute("""
        SELECT MIN(noise_level), MAX(noise_level)
        FROM measurements
        WHERE DATE(timestamp) = ?
    """, (date,))
    
    result = cursor.fetchone()
    conn.close()

    # Проверяем, есть ли данные
    if result and result[0] is not None and result[1] is not None:
        return result  # Кортеж (min_noise, max_noise)
    return None