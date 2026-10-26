package com.findr.service;

import com.findr.exception.*;
import com.findr.matching.Matchable;
import com.findr.model.*;
import com.findr.repository.FileStorage;
import com.findr.repository.Repository;
import com.findr.util.Validator;
import java.time.LocalDate;
import java.util.*;

/**
 * The BUSINESS LOGIC layer. Both the console app and the web server talk only to this class.
 * COMPOSITION: ItemService has-a two repositories, a file storage and a matcher.
 * DEPENDENCY INJECTION: those parts are passed in through the constructor, not created inside.
 * It depends on interfaces (Repository, Matchable), so the parts can be swapped.
 */
public class ItemService {
    public static final int MIN_SCORE = 60;              // pairs below this score are not shown

    private final Repository<LostItem> lostRepo;
    private final Repository<FoundItem> foundRepo;
    private final FileStorage storage;
    private Matchable matcher;                           // can be changed at run time (POLYMORPHISM)

    public ItemService(Repository<LostItem> lostRepo, Repository<FoundItem> foundRepo,
                       FileStorage storage, Matchable matcher) {
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.storage = storage;
        this.matcher = matcher;
    }

    // ---------- reporting ----------
    /** Validates the raw text, builds a LostItem object, stores and saves it. */
    public LostItem reportLost(String name, String category, String description, String location,
                               String date, String ownerName, String ownerContact) throws FindrException {
        String n = Validator.text("Item name", name);
        Category cat = Category.fromString(category);
        String desc = Validator.text("Description", description);
        String loc = Validator.text("Location", location);
        LocalDate when = Validator.date(date);
        String owner = Validator.text("Owner name", ownerName);
        String contact = Validator.contact(ownerContact);

        LostItem item = new LostItem(lostRepo.nextId(), n, cat, desc, loc, when, owner, contact);
        lostRepo.add(item);
        save();
        return item;
    }

    public FoundItem reportFound(String name, String category, String description, String location,
                                 String date, String finderName, String storedAt) throws FindrException {
        String n = Validator.text("Item name", name);
        Category cat = Category.fromString(category);
        String desc = Validator.text("Description", description);
        String loc = Validator.text("Location", location);
        LocalDate when = Validator.date(date);
        String finder = Validator.text("Finder name", finderName);
        String stored = Validator.text("Stored at", storedAt);

        FoundItem item = new FoundItem(foundRepo.nextId(), n, cat, desc, loc, when, finder, stored);
        foundRepo.add(item);
        save();
        return item;
    }

    /** METHOD OVERLOADING (compile-time polymorphism): same name add(), different parameter types. */
    public void add(LostItem item) throws StorageException { lostRepo.add(item); save(); }
    public void add(FoundItem item) throws StorageException { foundRepo.add(item); save(); }

    // ---------- reading ----------
    public Item findById(String id) throws ItemNotFoundException {
        try {
            return lostRepo.findById(id);
        } catch (ItemNotFoundException e) {
            return foundRepo.findById(id);             // not a lost item, so try the found items
        }
    }

    public List<LostItem> getLostItems() { return lostRepo.findAll(); }
    public List<FoundItem> getFoundItems() { return foundRepo.findAll(); }

    public List<Item> getAllItems() {
        List<Item> all = new ArrayList<Item>(lostRepo.findAll());    // upcasting: LostItem -> Item
        all.addAll(foundRepo.findAll());
        return all;
    }

    public boolean isEmpty() { return lostRepo.isEmpty() && foundRepo.isEmpty(); }

    // ---------- searching (OVERLOADED) ----------
    public List<Item> search(SearchCriteria criteria) {
        List<Item> result = new ArrayList<>();
        if (criteria.includesLost())
            for (LostItem i : lostRepo.findAll()) if (criteria.matches(i)) result.add(i);
        if (criteria.includesFound())
            for (FoundItem i : foundRepo.findAll()) if (criteria.matches(i)) result.add(i);
        return result;
    }

    /** Shortcut: search both lists for one keyword. */
    public List<Item> search(String keyword) {
        return search(new SearchCriteria("both", "", "", keyword, ""));
    }

    // ---------- updating ----------
    public Item updateStatus(String id, String status) throws FindrException {
        ItemStatus newStatus = ItemStatus.fromString(status);
        Item item = findById(id);
        item.setStatus(newStatus);
        save();
        return item;
    }

    public void delete(String id) throws FindrException {
        Item item = findById(id);
        if (item instanceof LostItem) lostRepo.remove(item.getId());      // instanceof: which list is it in?
        else foundRepo.remove(item.getId());
        save();
    }

    // ---------- matching (OVERLOADED) ----------
    /** All good matches for one lost item, highest score first. */
    public List<MatchResult> findMatches(LostItem lost) {
        List<MatchResult> results = new ArrayList<>();
        for (FoundItem found : foundRepo.findAll()) {
            if (found.getStatus() != ItemStatus.ACTIVE) continue;
            int score = matcher.calculateScore(lost, found);   // POLYMORPHISM: whichever Matchable is plugged in
            if (score >= MIN_SCORE) results.add(new MatchResult(lost, found, score));
        }
        Collections.sort(results);                              // uses MatchResult.compareTo (Comparable)
        return results;
    }

    /** Matches for every active lost item. */
    public List<MatchResult> findMatches() {
        List<MatchResult> all = new ArrayList<>();
        for (LostItem lost : lostRepo.findAll())
            if (lost.getStatus() == ItemStatus.ACTIVE) all.addAll(findMatches(lost));
        Collections.sort(all);
        return all;
    }

    public Matchable getMatcher() { return matcher; }
    public void setMatcher(Matchable matcher) { this.matcher = matcher; }

    private void save() throws StorageException {
        storage.save(lostRepo.findAll(), foundRepo.findAll());
    }
}
