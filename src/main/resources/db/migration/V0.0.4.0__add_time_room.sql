ALTER TABLE game_room
    add column game_time int;

COMMENT ON COLUMN game_room.game_time                  IS 'Время игры';