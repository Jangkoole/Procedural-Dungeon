package com.dungeon.service;

import com.dungeon.model.GameSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    @Value("${game.session-timeout-minutes:30}")
    private int timeoutMinutes;

    public String createId() {
        return UUID.randomUUID().toString();
    }

    public void put(GameSession session) {
        sessions.put(session.getSessionId(), session);
    }

    public GameSession get(String sessionId) {
        return sessions.get(sessionId);
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public Collection<GameSession> getAll() {
        return sessions.values();
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredSessions() {
        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        sessions.entrySet().removeIf(entry -> entry.getValue().getLastActiveAt().isBefore(cutoff));
    }
}
