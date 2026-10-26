package com.findr.service;

import com.findr.model.Item;

/**
 * Holds the filters for a search. ENCAPSULATION: the class itself knows how to test an item (matches()),
 * so the rest of the program just asks "does this item match?".
 */
public class SearchCriteria {
    private final String type;       // "lost", "found" or "both"
    private final String category;
    private final String location;
    private final String keyword;
    private final String date;       // YYYY-MM-DD or empty

    public SearchCriteria(String type, String category, String location, String keyword, String date) {
        this.type = clean(type).isEmpty() ? "both" : clean(type).toLowerCase();
        this.category = clean(category);
        this.location = clean(location);
        this.keyword = clean(keyword);
        this.date = clean(date);
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }

    public boolean includesLost() { return !type.equals("found"); }
    public boolean includesFound() { return !type.equals("lost"); }

    /** Blank filters are ignored. Text is compared in lowercase. */
    public boolean matches(Item item) {
        if (!category.isEmpty() && !item.getCategory().getLabel().equalsIgnoreCase(category)) return false;
        if (!location.isEmpty() && !item.getLocation().toLowerCase().contains(location.toLowerCase())) return false;
        if (!date.isEmpty() && !item.getDate().toString().equals(date)) return false;
        if (!keyword.isEmpty()) {
            String text = (item.getName() + " " + item.getDescription()).toLowerCase();
            if (!text.contains(keyword.toLowerCase())) return false;
        }
        return true;
    }
}
