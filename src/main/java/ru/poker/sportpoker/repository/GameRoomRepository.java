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
             left join fetch gr.gameRoomPlayers
            """)
    Optional<GameRoom> findGameRoomWithPlayers(UUID roomId);

    @Query(value = """
            select gr from GameRoom gr
            left join fetch gr.gameRoomPlayers
            where gr.status = :statusGame
            """)
    List<GameRoom> findGameRoomByStatusEquals(StatusGame statusGame);

    @Query(value = """
               select case when (COUNT (gr) > 0) THEN true ELSE false END 
               from GameRoom gr
               left join gr.gameRoomPlayers pl 
               where gr.id = :gameRoomId 
               and (pl.playersId = :userId or gr.creator = :userId)
            """)
    boolean userFromThisRoom(UUID userId, UUID roomId);

    @Query(value = """
               select case when (COUNT (gr) > 0) THEN true ELSE false END 
               from GameRoom gr
               left join gr.gameRoomPlayers pl 
               where gr.id = :gameRoomId 
               and gr.creator = :userId
            """)
    boolean userIsCreatorRoom(UUID userId, UUID roomId);

    @Query(value = """
               select case when (COUNT (gr) > 0) THEN true ELSE false END 
               from GameRoom gr
               left join gr.gameRoomPlayers pl 
               where gr.id = :gameRoomId 
               and pl.playersId = :userId
            """)
    boolean userIsPlayerRoom(UUID userId, UUID roomId);

    @Query(value = """
               select case when (COUNT (gr) > 0) THEN true ELSE false END 
               from GameRoom gr
               left join gr.gameRoomPlayers pl 
               where (pl.playersId = :userId or gr.creator = :userId)
               and gr.status in :statusGame
            """)
    boolean userHasRoom(UUID userId, List<StatusGame> statusGame);

    boolean existsByName(String name);

    @Query(value = """
                 select gr from GameRoom gr
                 where gr.name = :name
                 and gr.id <> :gameRoomId
            """)
    boolean existsByName(String name, UUID roomId);
}
