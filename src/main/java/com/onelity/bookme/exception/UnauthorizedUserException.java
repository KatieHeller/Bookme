package com.onelity.bookme.exception;

public class UnauthorizedUserException extends Exception {
    public UnauthorizedUserException(String errorMessage) {
        super(errorMessage);
    }
}
