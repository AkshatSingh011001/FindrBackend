package com.findr.exception;

/** Thrown when reading or writing the CSV files fails. */
public class StorageException extends FindrException {
    private static final long serialVersionUID = 1L;
    public StorageException(String message) { super(message); }
}
