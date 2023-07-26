package com.onelity.bookme.exception;

public class InvalidBookingException extends Exception {
    public InvalidBookingException(String errorMessage) {
        super(errorMessage);
    }
}
