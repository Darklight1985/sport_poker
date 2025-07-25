package ru.poker.sportpoker.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@UtilityClass
public class TokenUtils {

    @Value("${application.join-token.secret}")
    private String secretKey;

    @Value("${application.current-domain}")
    String address;

    @Value("${server.port}")
    String port;

    private final static String ROOM_ID = "roomId";
    private final static String URL_FORMAT = "http://%s:%s/room/join/";

    public String getRoomId(String token) throws JwtException {
        String roomId;
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
        roomId = claims.get(ROOM_ID, String.class);
        return roomId;
    }

    public String getLinkWithToken(UUID id) {
        return URL_FORMAT.formatted(address, port) + Jwts.builder()
                .claim(ROOM_ID, id)
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
