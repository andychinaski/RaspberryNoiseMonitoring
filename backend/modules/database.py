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