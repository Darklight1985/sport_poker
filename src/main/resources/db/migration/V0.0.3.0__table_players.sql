-- Создание таблицы участников комнаты

alter table game_room
add constraint pk_game_room PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS game_room_players (
                                         game_room_id        uuid   NOT NULL,
                                         players_id          uuid   NOT NULL,
    CONSTRAINT unique_game_room_players UNIQUE (game_room_id, players_id),
    constraint fk_room FOREIGN KEY (game_room_id) references game_room(id)
);

COMMENT ON TABLE game_room_players                     IS 'Таблица со списком участников игровой комнаты';
COMMENT ON COLUMN game_room_players.game_room_id       IS 'Id комнаты';
COMMENT ON COLUMN game_room_players.players_id         IS 'Id участника';
