package com.dungeon.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private String sessionId;
    private Dungeon dungeon;
    private Player player;
    private GameStatus status;
    private int turn;
    private Instant createdAt;
    private Instant lastActiveAt;
    private List<String> messages;

    public GameSession() {}

    public GameSession(String sessionId, Dungeon dungeon, Player player) {
        this.sessionId = sessionId;
        this.dungeon = dungeon;
        this.player = player;
        this.status = GameStatus.PLAYING;
        this.turn = 0;
        this.createdAt = Instant.now();
        this.lastActiveAt = Instant.now();
        this.messages = new ArrayList<>();
    }

    public void addMessage(String msg) {
        messages.add(msg);
        if (messages.size() > 5) {
            messages.remove(0);
        }
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }

    public int remainingMonsters() {
        return (int) dungeon.getMonsters().stream().filter(Monster::isAlive).count();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Dungeon getDungeon() { return dungeon; }
    public void setDungeon(Dungeon dungeon) { this.dungeon = dungeon; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public int getTurn() { return turn; }
    public void setTurn(int turn) { this.turn = turn; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(Instant lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }
}
