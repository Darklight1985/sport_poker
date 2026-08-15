package ru.poker.sportpoker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameRoomSseService {

    /**
     * Карта: roomId -> список эмиттеров подписчиков
     */
    private final Map<UUID, List<SseEmitter>> roomEmitters = new ConcurrentHashMap<>();

    /**
     * Карта: playerId -> эмиттер для персональной рассылки
     */
    private final Map<UUID, SseEmitter> playerEmitters = new ConcurrentHashMap<>();

    /**
     * Добавляет клиента в подписку на события комнаты.
     */
    public SseEmitter addSubscriber(UUID roomId, UUID playerId) {
        SseEmitter emitter = new SseEmitter(60_000L); // 60 секунд таймаут

        // Добавляем в список эмиттеров комнаты
        roomEmitters.computeIfAbsent(roomId, k -> new ArrayList<>()).add(emitter);

        // Сохраняем персональную подписку игрока
        playerEmitters.put(playerId, emitter);

        // Обработка завершения/таймаута
        emitter.onCompletion(() -> removeSubscriber(roomId, playerId, emitter));
        emitter.onTimeout(() -> removeSubscriber(roomId, playerId, emitter));
        emitter.onError(e -> removeSubscriber(roomId, playerId, emitter));

        log.info("Игрок {} подписан на события комнаты {}", playerId, roomId);
        return emitter;
    }

    /**
     * Удаляет эмиттер из подписки.
     */
    private void removeSubscriber(UUID roomId, UUID playerId, SseEmitter emitter) {
        playerEmitters.remove(playerId);
        List<SseEmitter> emitters = roomEmitters.get(roomId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
        log.info("Игрок {} отписан от комнаты {}", playerId, roomId);
    }

    /**
     * Рассылает событие всем подписчикам комнаты.
     */
    public void sendToRoom(UUID roomId, String eventName, Object data) {
        List<SseEmitter> emitters = roomEmitters.get(roomId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("Нет подписчиков для комнаты {}", roomId);
            return;
        }

        String dataStr = data.toString();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(dataStr));
            } catch (IOException e) {
                log.error("Ошибка отправки события {} в комнату {}", eventName, roomId, e);
            }
        }
        log.info("Отправлено событие {} в комнату {}", eventName, roomId);
    }

    /**
     * Рассылает событие конкретному игроку.
     */
    public void sendToPlayer(UUID roomId, UUID playerId, String eventName, Object data) {
        SseEmitter emitter = playerEmitters.get(playerId);
        if (emitter == null) {
            log.debug("Игрок {} не подписан", playerId);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
            log.info("Отправлено событие {} игроку {}", eventName, playerId);
        } catch (IOException e) {
            log.error("Ошибка отправки события {} игроку {}", eventName, playerId, e);
        }
    }

    /**
     * Удаляет все эмиттеры для комнаты (при завершении игры).
     */
    public void removeRoomSubscribers(UUID roomId) {
        List<SseEmitter> emitters = roomEmitters.remove(roomId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                emitter.complete();
            }
            log.info("Все эмиттеры комнаты {} удалены", roomId);
        }
    }
}
