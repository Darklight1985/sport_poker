package ru.poker.sportpoker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.enums.StatusGame;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRoomRepository extends CrudRepository<GameRoom, UUID> {

    @Query(value = """
            select gr from GameRoom gr
             left join fetch gr.players
            """)
    Optional<GameRoom> findGameRoomWithPlayers(UUID roomId);

    List<GameRoom> findGameRoomByStatusEquals(StatusGame statusGame);
}
