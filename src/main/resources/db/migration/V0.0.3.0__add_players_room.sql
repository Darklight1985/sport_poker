ALTER TABLE game_room
add column players jsonb default '[]';

COMMENT ON COLUMN game_room.players                    IS 'Участники игровой комнаты';