package com.findr.exception;

/** Thrown when an item ID does not exist. */
public class ItemNotFoundException extends FindrException {
    private static final long serialVersionUID = 1L;
    public ItemNotFoundException(String id) { super("No item found with ID: " + id); }
}
