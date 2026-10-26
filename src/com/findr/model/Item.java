package com.findr.model;

import com.findr.util.Json;
import java.time.LocalDate;

/**
 * ABSTRACTION: abstract parent of LostItem and FoundItem. It cannot be instantiated directly.
 * ENCAPSULATION: every field is private; only getters (and one setter, for status) are public.
 * INTERFACE: implements JsonSerializable.
 */
public abstract class Item implements JsonSerializable {
    private final String id;
    private final String name;
    private final Category category;       // COMPOSITION: an Item has-a Category (enum)
    private final String description;
    private final String location;
    private final LocalDate date;          // COMPOSITION: an Item has-a LocalDate
    private ItemStatus status;             // the only field that may change after creation

    /** Full constructor. */
    protected Item(String id, String name, Category category, String description,
                   String location, LocalDate date, ItemStatus status) {
        this.id = id; this.name = name; this.category = category;
        this.description = description; this.location = location;
        this.date = date; this.status = status;
    }

    /** CONSTRUCTOR OVERLOADING + this(): a shorter constructor that defaults the status to ACTIVE. */
    protected Item(String id, String name, Category category, String description,
                   String location, LocalDate date) {
        this(id, name, category, description, location, date, ItemStatus.ACTIVE);
    }

    // ---- getters (read-only access) ----
    public String getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    // ---- ABSTRACT methods: each child class MUST provide its own version ----
    public abstract String getType();          // "lost" or "found"
    public abstract String getPartyName();     // owner name (lost) / finder name (found)
    public abstract String getPartyDetail();   // owner contact (lost) / storage place (found)
    public abstract String getContactInfo();   // readable contact sentence
    public abstract String toCsv();            // one line for the CSV file

    /** Normal (non-abstract) method that children reuse through super.displayDetails(). */
    public void displayDetails() {
        System.out.println("ID: " + id + " | " + name + " | Category: " + category);
        System.out.println("   Description: " + description);
        System.out.println("   Location: " + location + " | Date: " + date + " | Status: " + status);
    }

    /** Uses the abstract getters, so it automatically works for both lost and found items (polymorphism). */
    @Override
    public String toJson() {
        return "{\"t\":" + Json.quote(getType()) + ",\"id\":" + Json.quote(id)
             + ",\"name\":" + Json.quote(name) + ",\"cat\":" + Json.quote(category.getLabel())
             + ",\"desc\":" + Json.quote(description) + ",\"loc\":" + Json.quote(location)
             + ",\"date\":" + Json.quote(date.toString()) + ",\"st\":" + Json.quote(status.name())
             + ",\"p1\":" + Json.quote(getPartyName()) + ",\"p2\":" + Json.quote(getPartyDetail()) + "}";
    }

    /** protected: only Item and its children may use this helper. */
    protected String baseCsv() {
        return String.join("|", id, name, category.getLabel(), description, location, date.toString(), status.name());
    }

    // ---- OBJECT methods overridden: two items are "equal" when their IDs are equal ----
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        return id.equals(((Item) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return getType().toUpperCase() + " " + id + " (" + name + ")"; }
}
