package com.findr;

import com.findr.exception.FindrException;
import com.findr.matching.StrictMatcher;
import com.findr.matching.WeightedMatcher;
import com.findr.model.*;
import com.findr.service.ItemService;
import com.findr.service.SearchCriteria;
import com.findr.service.ServiceFactory;
import java.util.List;
import java.util.Scanner;

/** Console version of the Campus Lost & Found Matching System. It is thin: all logic is in ItemService. */
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static ItemService service;

    public static void main(String[] args) {
        try {
            service = ServiceFactory.create("data");
        } catch (FindrException e) {
            System.out.println("Could not start: " + e.getMessage());
            return;
        }
        System.out.println("=== CAMPUS LOST & FOUND MATCHING SYSTEM ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": reportLost(); break;
                    case "2": reportFound(); break;
                    case "3": showAll(service.getLostItems()); break;
                    case "4": showAll(service.getFoundItems()); break;
                    case "5": searchItems(); break;
                    case "6": matchForOne(); break;
                    case "7": showMatches(service.findMatches()); break;
                    case "8": updateStatus(); break;
                    case "9": deleteItem(); break;
                    case "10": changeMode(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Enter a number from the menu.");
                }
            } catch (FindrException e) {           // one catch handles every custom exception
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        System.out.println("Data saved in the data/ folder. Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n 1. Report LOST item          6. Find matches for a lost item");
        System.out.println(" 2. Report FOUND item         7. Show ALL possible matches");
        System.out.println(" 3. View all LOST items       8. Mark item CLAIMED / RETURNED");
        System.out.println(" 4. View all FOUND items      9. Delete an item");
        System.out.println(" 5. Search items             10. Change matching mode (now: " + service.getMatcher().getName() + ")");
        System.out.println(" 0. Exit");
        System.out.print("Enter choice: ");
    }

    private static String ask(String label) { System.out.print(label + ": "); return sc.nextLine().trim(); }

    private static void reportLost() throws FindrException {
        System.out.println("Categories: " + Category.allLabels());
        LostItem item = service.reportLost(ask("Item name"), ask("Category"), ask("Description"),
                ask("Where was it lost"), ask("Date (YYYY-MM-DD)"), ask("Your name"), ask("Your contact (10-digit phone or email)"));
        System.out.println("Lost item saved with ID " + item.getId());
        List<MatchResult> matches = service.findMatches(item);
        if (!matches.isEmpty()) { System.out.println("Good news! Possible matches already found:"); showMatches(matches); }
    }

    private static void reportFound() throws FindrException {
        System.out.println("Categories: " + Category.allLabels());
        FoundItem item = service.reportFound(ask("Item name"), ask("Category"), ask("Description"),
                ask("Where was it found"), ask("Date (YYYY-MM-DD)"), ask("Finder name"), ask("Stored at (e.g. Security Office)"));
        System.out.println("Found item saved with ID " + item.getId());
    }

    /** "? extends Item": one method prints a list of LostItem or a list of FoundItem. */
    private static void showAll(List<? extends Item> items) {
        if (items.isEmpty()) System.out.println("No items to show.");
        for (Item item : items) item.displayDetails();      // POLYMORPHISM: each object prints itself
    }

    private static void searchItems() {
        SearchCriteria criteria = new SearchCriteria(ask("Search in (lost / found / both)"),
                ask("Category (Enter to skip)"), ask("Location (Enter to skip)"),
                ask("Keyword (Enter to skip)"), ask("Date YYYY-MM-DD (Enter to skip)"));
        List<Item> results = service.search(criteria);
        System.out.println(results.size() + " result(s):");
        showAll(results);
    }

    private static void matchForOne() throws FindrException {
        Item item = service.findById(ask("Enter LOST item ID (e.g. L1)"));
        if (!(item instanceof LostItem)) { System.out.println("That ID is not a lost item."); return; }
        List<MatchResult> matches = service.findMatches((LostItem) item);     // downcast after instanceof check
        if (matches.isEmpty()) System.out.println("No matches with score " + ItemService.MIN_SCORE + " or above yet.");
        showMatches(matches);
    }

    private static void showMatches(List<MatchResult> matches) {
        System.out.println(matches.size() + " possible match(es):");
        for (MatchResult m : matches) m.display();
    }

    private static void updateStatus() throws FindrException {
        service.updateStatus(ask("Item ID"), ask("New status (CLAIMED / RETURNED / ACTIVE)"));
        System.out.println("Status updated.");
    }

    private static void deleteItem() throws FindrException {
        service.delete(ask("Item ID to delete"));
        System.out.println("Item deleted.");
    }

    /** Live demo of polymorphism: swap the matching algorithm without touching any other code. */
    private static void changeMode() {
        String c = ask("1 = Weighted (default), 2 = Strict (category AND location must match)");
        service.setMatcher(c.equals("2") ? new StrictMatcher() : new WeightedMatcher());
        System.out.println("Matching mode is now: " + service.getMatcher().getName());
    }
}
