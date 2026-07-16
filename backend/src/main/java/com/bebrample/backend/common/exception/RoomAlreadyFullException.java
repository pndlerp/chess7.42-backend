package com.bebrample.backend.common.exception;


public class RoomAlreadyFullException extends RuntimeException {
    public RoomAlreadyFullException(String message) {
        super(message);
    }
}
