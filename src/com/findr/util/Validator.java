package com.findr.util;

import com.findr.exception.InvalidInputException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Checks text typed by the user. Throws InvalidInputException with a clear message when it is wrong. */
public final class Validator {
    private Validator() { }

    /** METHOD OVERLOADING: same name, different parameters. This one uses the default limit of 200 characters. */
    public static String text(String label, String value) throws InvalidInputException {
        return text(label, value, 200);
    }

    /** Required text with a custom maximum length. The | character is banned because it separates CSV fields. */
    public static String text(String label, String value, int maxLength) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) throw new InvalidInputException(label + " cannot be empty.");
        String v = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (v.contains("|")) throw new InvalidInputException("The character | is not allowed in " + label + ".");
        if (v.length() > maxLength) throw new InvalidInputException(label + " is too long (max " + maxLength + " characters).");
        return v;
    }

    public static LocalDate date(String d) throws InvalidInputException {
        try {
            return LocalDate.parse(d == null ? "" : d.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date must be a real date in YYYY-MM-DD format.");
        }
    }

    public static String contact(String c) throws InvalidInputException {
        String v = text("Contact", c);
        if (!v.matches("\\d{10}") && !v.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+"))
            throw new InvalidInputException("Contact must be a 10-digit phone number or a valid email.");
        return v;
    }
}
