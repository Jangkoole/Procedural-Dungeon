package com.dungeon.exception;

public class InvalidActionException extends RuntimeException {
    private final String errorCode;

    public InvalidActionException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
