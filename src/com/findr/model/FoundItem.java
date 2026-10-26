package com.findr.model;

import java.time.LocalDate;

/** INHERITANCE: a FoundItem "is an" Item. It adds the finder's name and where the item is kept. */
public class FoundItem extends Item {
    private final String finderName;
    private final String storedAt;

    public FoundItem(String id, String name, Category category, String description, String location,
                     LocalDate date, ItemStatus status, String finderName, String storedAt) {
        super(id, name, category, description, location, date, status);
        this.finderName = finderName;
        this.storedAt = storedAt;
    }

    /** CONSTRUCTOR OVERLOADING (status defaults to ACTIVE). */
    public FoundItem(String id, String name, Category category, String description, String location,
                     LocalDate date, String finderName, String storedAt) {
        this(id, name, category, description, location, date, ItemStatus.ACTIVE, finderName, storedAt);
    }

    @Override public String getType() { return "found"; }
    @Override public String getPartyName() { return finderName; }
    @Override public String getPartyDetail() { return storedAt; }
    @Override public String getContactInfo() { return "Found by: " + finderName + " | Collect from: " + storedAt; }
    @Override public String toCsv() { return "FOUND|" + baseCsv() + "|" + finderName + "|" + storedAt; }

    /** POLYMORPHISM (overriding): found items print themselves their own way. */
    @Override public void displayDetails() {
        System.out.println("[FOUND]");
        super.displayDetails();
        System.out.println("   " + getContactInfo());
        System.out.println("--------------------------------------------");
    }
}
