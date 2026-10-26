package com.findr.model;

import com.findr.exception.InvalidInputException;

/** ENUM: a fixed list of allowed categories. An enum cannot hold a misspelt value like "Electonics". */
public enum Category {
    ELECTRONICS("Electronics"), ACCESSORIES("Accessories"), STATIONERY("Stationery"), BOOKS("Books"),
    PERSONAL("Personal"), CLOTHING("Clothing"), CARDS("Cards"), OTHER("Other");

    private final String label;                       // each constant carries its display text

    Category(String label) { this.label = label; }    // enum constructor

    public String getLabel() { return label; }

    @Override public String toString() { return label; }

    /** Converts text typed by the user into a Category (case-insensitive). */
    public static Category fromString(String text) throws InvalidInputException {
        if (text != null)
            for (Category c : values())
                if (c.label.equalsIgnoreCase(text.trim())) return c;
        throw new InvalidInputException("Category must be one of: " + allLabels());
    }

    public static String allLabels() {
        StringBuilder sb = new StringBuilder();
        for (Category c : values()) { if (sb.length() > 0) sb.append(", "); sb.append(c.label); }
        return sb.toString();
    }
}
