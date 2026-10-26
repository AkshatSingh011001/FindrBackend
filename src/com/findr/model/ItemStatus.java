package com.findr.model;

import com.findr.exception.InvalidInputException;

/** ENUM: the life cycle of an item. */
public enum ItemStatus {
    ACTIVE, CLAIMED, RETURNED;

    public static ItemStatus fromString(String text) throws InvalidInputException {
        if (text != null)
            for (ItemStatus s : values())
                if (s.name().equalsIgnoreCase(text.trim())) return s;
        throw new InvalidInputException("Status must be ACTIVE, CLAIMED or RETURNED.");
    }
}
