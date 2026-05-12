package com.dungeon.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String sessionId) {
        super("Session " + sessionId + " not found or expired.");
    }
}
