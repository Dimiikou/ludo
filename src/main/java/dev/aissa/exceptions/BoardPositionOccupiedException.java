package dev.aissa.exceptions;

public class BoardPositionOccupiedException extends RuntimeException {

    public BoardPositionOccupiedException(String message) {
        super(message);
    }
}
