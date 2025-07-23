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

    public String getRoomId(String token) throws JwtException {
        String roomId;
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
        roomId = claims.get("roomId", String.class);
        return roomId;
    }

    public String getLinkWithToken(UUID id) {
        return "http://" + address + ":" + port + "/room/join/" + Jwts.builder()
                .claim("roomId", id)
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
