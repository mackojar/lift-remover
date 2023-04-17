package com.mjds.liftremover.exceptions;

public class FitFileException extends Exception {

    public FitFileException() {
    }

    public FitFileException(String message) {
        super(message);
    }

    public FitFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FitFileException(Throwable cause) {
        super(cause);
    }
}
