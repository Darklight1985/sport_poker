package ru.poker.sportpoker.repository;

import org.springframework.data.repository.CrudRepository;
import ru.poker.sportpoker.domain.GameRoom;

import java.util.UUID;

public interface GameRoomRepository extends CrudRepository<GameRoom, UUID> {
}
