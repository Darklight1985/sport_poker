-- Создание расширения для генерации uuid-ов
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Создание таблицы экспериментов
CREATE TABLE IF NOT EXISTS game_room (
    id                  uuid                       DEFAULT uuid_generate_v4()  NOT NULL,
    name                CHARACTER VARYING(255),
    created             TIMESTAMP WITH TIME ZONE,
    updated             TIMESTAMP WITH TIME ZONE,
    status              varchar(15)                                            NOT NULL
    );

COMMENT ON TABLE game_room                     IS 'Таблица со списком игровых комнат';
COMMENT ON COLUMN game_room.id                 IS 'Id комнаты';
COMMENT ON COLUMN game_room.name               IS 'Имя комнаты';
COMMENT ON COLUMN game_room.created            IS 'Время создания комнаты';
COMMENT ON COLUMN game_room.updated            IS 'Время последнего обновления комнаты';
COMMENT ON COLUMN game_room.status             IS 'Статус комнаты';
