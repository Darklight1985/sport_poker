ALTER TABLE game_room_players
    add column id uuid default uuid_generate_v4();

ALTER TABLE game_room_players
    add column ready boolean default false;

alter table game_room_players
    add constraint pk_game_room_players PRIMARY KEY (id);

COMMENT ON COLUMN game_room_players.id                  IS 'Идентификатор связи пользователя и комнаты';
COMMENT ON COLUMN game_room_players.ready               IS 'Флаг указывающий что пользоватль готов к игре';