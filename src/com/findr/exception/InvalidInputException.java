package com.findr.exception;

/** Thrown when the user gives bad data (empty name, impossible date, wrong phone number...). */
public class InvalidInputException extends FindrException {
    private static final long serialVersionUID = 1L;
    public InvalidInputException(String message) { super(message); }
}
