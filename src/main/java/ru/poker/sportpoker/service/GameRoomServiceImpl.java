package ru.poker.sportpoker.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.repository.GameRoomRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;

    @Override
    public void createGameRoom(CreateGameRoomDto dto) {
        keycloakUserService.getCurrentUser();
        GameRoom gameRoom = new GameRoom();
        gameRoom.setName(dto.getName());

    }

    @Override
    public GameRoom getGameRoom(UUID id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString()));
    }

    @Override
    public void updateGameRoom(UUID id, GameRoom gameRoom) {

    }

    @Override
    public void deleteGameRoom(UUID id) {
        gameRoomRepository.deleteById(id);
    }
}
