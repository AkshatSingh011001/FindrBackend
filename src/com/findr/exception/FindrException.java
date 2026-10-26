package com.findr.exception;

/**
 * INHERITANCE of exceptions: the parent of all our custom exceptions.
 * Catching FindrException catches every project-specific error in one go.
 */
public class FindrException extends Exception {
    private static final long serialVersionUID = 1L;
    public FindrException(String message) { super(message); }
}
