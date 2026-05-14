package com.dungeon.dto;

import com.dungeon.model.GameSession;
import com.dungeon.model.GameStatus;

import java.time.Instant;
import java.util.List;

public class SessionListResponse {
    private List<SessionInfo> sessions;
    private int count;

    public SessionListResponse(List<SessionInfo> sessions) {
        this.sessions = sessions;
        this.count = sessions.size();
    }

    public static class SessionInfo {
        public String sessionId;
        public Instant createdAt;
        public Instant lastActiveAt;
        public int turn;
        public GameStatus status;

        public static SessionInfo from(GameSession s) {
            SessionInfo info = new SessionInfo();
            info.sessionId = s.getSessionId();
            info.createdAt = s.getCreatedAt();
            info.lastActiveAt = s.getLastActiveAt();
            info.turn = s.getTurn();
            info.status = s.getStatus();
            return info;
        }
    }

    public List<SessionInfo> getSessions() { return sessions; }
    public int getCount() { return count; }
}
