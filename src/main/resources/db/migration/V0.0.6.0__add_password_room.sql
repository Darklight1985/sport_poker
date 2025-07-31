ALTER TABLE game_room
add column password varchar(15) default 'QWE123456',
add column version bigint default 1;

COMMENT ON COLUMN game_room.password                    IS 'Пароль от комнаты';
COMMENT ON COLUMN game_room.version                     IS 'Версия сущности';