insert into game_room (id, name, created, updated, status, creator, game_time, password, version)
values ('327d8a5d-7408-4f8d-99aa-b2fcdc39587d'::uuid, 'room1', '2025-08-09 14:30:00+03', '2025-08-09 14:30:00+03', 'PREP', '227d8a5d-7408-4f8d-99aa-b2fcdc39587d'::uuid, 20, 1234567, 0),
('327d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, 'room2', '2025-08-09 14:30:00+03', '2025-08-09 14:30:00+03', 'PREP', '227d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, 20, 1234567, 0);

insert into game_room_players (game_room_id, players_id, id, ready)
values ('327d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, '227d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, '127d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, true),
('327d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, '227d8a5d-7408-4f8d-99aa-b2fcdc39587f'::uuid, '127d8a5d-7408-4f8d-99aa-b2fcdc39587f'::uuid, true),
('327d8a5d-7408-4f8d-99aa-b2fcdc39587e'::uuid, '227d8a5d-7408-4f8d-99aa-b2fcdc39587c'::uuid, '127d8a5d-7408-4f8d-99aa-b2fcdc39587c'::uuid, false);