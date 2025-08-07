-- Создание таблицы c аватарами

CREATE TABLE IF NOT EXISTS avatars (
                                                 id               uuid   NOT NULL,
                                                 user_id          uuid   NOT NULL,
                                                 CONSTRAINT unique_avatar_user UNIQUE (id, user_id)
);

COMMENT ON TABLE avatars                     IS 'Таблица со списком аватарок пользователей';
COMMENT ON COLUMN avatars.id                 IS 'Id аватара';
COMMENT ON COLUMN avatars.user_id            IS 'Id игрока';
