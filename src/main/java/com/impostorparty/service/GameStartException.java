package com.impostorparty.service;

public class GameStartException extends Exception {
    private final int statusCode;

    public GameStartException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
