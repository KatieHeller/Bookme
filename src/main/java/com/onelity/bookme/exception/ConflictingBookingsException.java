package com.onelity.bookme.exception;

public class ConflictingBookingsException extends Exception {
    public ConflictingBookingsException(String errorMessage) {
        super(errorMessage);
    }
}
