CREATE TABLE IF NOT EXISTS room_exercises
(
    game_room_id uuid        NOT NULL,
    exercise     varchar(15) NOT NULL,
    CONSTRAINT unique_exercises_room UNIQUE (game_room_id, exercise),
    constraint fk_room FOREIGN KEY (game_room_id) references game_room (id)
);

COMMENT ON TABLE room_exercises IS 'Таблица со списком упражнений игровой комнаты';
COMMENT ON COLUMN room_exercises.game_room_id IS 'Id комнаты';
COMMENT ON COLUMN room_exercises.exercise IS 'Название упражнения';