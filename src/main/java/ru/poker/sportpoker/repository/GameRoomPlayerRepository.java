package ru.poker.sportpoker.repository;

import org.springframework.data.repository.CrudRepository;
import ru.poker.sportpoker.domain.GameRoomPlayer;

import java.util.Optional;
import java.util.UUID;

public interface GameRoomPlayerRepository extends CrudRepository<GameRoomPlayer, UUID> {

    Optional<GameRoomPlayer> findByPlayersId(UUID id);



}
