ALTER TABLE game_room
add column creator uuid;

create unique index game_room_name on game_room(name);
create unique index game_room_user on game_room(creator);

COMMENT ON COLUMN game_room.creator                     IS 'Создатель комнаты';