package ru.poker.sportpoker.validate.room;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.KeycloakUserService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserIsPlayerHandlerByToken extends RoomHandler<String> {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;

    @Value("${application.join-token.secret}")
    private String secretKey;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, String... uuids) {
        if (uuids == null || uuids.length == 0) {
            bindingResult.reject("uuid", "must not be empty");
            return;
        }

        String token = uuids[0];
        String user = keycloakUserService.getCurrentUser();
        if (user == null) {
            bindingResult.reject("user", "user.not.found");
        }
        if (token == null) {
            bindingResult.reject("roomId", "user.not.found");
        }

        if (bindingResult.hasErrors()) {
            return;
        }

        UUID userId = UUID.fromString(user);
        String roomId;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
            roomId = claims.get("roomId", String.class);
        } catch (JwtException e) {
            bindingResult.reject("Invalid or expired link");
            return;
        }

        if (!gameRoomRepository.userIsPlayerRoom(userId, UUID.fromString(roomId))) {
            bindingResult.reject("user", "Пользователь %s не является участником комнаты %s".formatted(user, uuids));
        }
    }
}
