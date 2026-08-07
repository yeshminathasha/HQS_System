package com.smarthospital.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("An account already exists with email: " + email);
    }
}
