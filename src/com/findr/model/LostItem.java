package com.findr.model;

import java.time.LocalDate;

/** INHERITANCE: a LostItem "is an" Item. It adds only what is different: the owner's details. */
public class LostItem extends Item {
    private final String ownerName;
    private final String ownerContact;

    public LostItem(String id, String name, Category category, String description, String location,
                    LocalDate date, ItemStatus status, String ownerName, String ownerContact) {
        super(id, name, category, description, location, date, status);   // super(...) builds the Item part
        this.ownerName = ownerName;
        this.ownerContact = ownerContact;
    }

    /** CONSTRUCTOR OVERLOADING: same class, shorter parameter list (status defaults to ACTIVE). */
    public LostItem(String id, String name, Category category, String description, String location,
                    LocalDate date, String ownerName, String ownerContact) {
        this(id, name, category, description, location, date, ItemStatus.ACTIVE, ownerName, ownerContact);
    }

    @Override public String getType() { return "lost"; }
    @Override public String getPartyName() { return ownerName; }
    @Override public String getPartyDetail() { return ownerContact; }
    @Override public String getContactInfo() { return "Owner: " + ownerName + " (" + ownerContact + ")"; }
    @Override public String toCsv() { return "LOST|" + baseCsv() + "|" + ownerName + "|" + ownerContact; }

    /** POLYMORPHISM (overriding): lost items print themselves their own way. */
    @Override public void displayDetails() {
        System.out.println("[LOST]");
        super.displayDetails();
        System.out.println("   " + getContactInfo());
        System.out.println("--------------------------------------------");
    }
}
