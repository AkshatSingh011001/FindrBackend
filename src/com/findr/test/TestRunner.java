package com.findr.test;

import com.findr.exception.*;
import com.findr.matching.*;
import com.findr.model.*;
import com.findr.repository.*;
import com.findr.service.*;
import com.findr.util.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Simple self-made test runner (no JUnit needed). Run it to prove every feature works.
 * Each check prints PASS or FAIL, and the program ends with a summary.
 */
public class TestRunner {
    private static int passed = 0, failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  PASS  " + name); }
        else    { failed++; System.out.println("  FAIL  " + name); }
    }

    /** Runs code that SHOULD throw an InvalidInputException. */
    private interface Risky { void run() throws Exception; }
    private static boolean throwsInvalid(Risky r) {
        try { r.run(); return false; } catch (InvalidInputException e) { return true; } catch (Exception e) { return false; }
    }

    private static LostItem lost(String id, String name, Category c, String desc, String loc, String date) {
        return new LostItem(id, name, c, desc, loc, LocalDate.parse(date), "Owner", "9876543210");
    }
    private static FoundItem found(String id, String name, Category c, String desc, String loc, String date) {
        return new FoundItem(id, name, c, desc, loc, LocalDate.parse(date), "Finder", "Office");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== Validation and enums ==");
        check("bad date rejected", throwsInvalid(() -> Validator.date("2026-13-45")));
        check("good date accepted", Validator.date("2026-10-20").equals(LocalDate.parse("2026-10-20")));
        check("10-digit phone accepted", Validator.contact("9876543210").equals("9876543210"));
        check("email accepted", Validator.contact("a@college.edu").equals("a@college.edu"));
        check("bad contact rejected", throwsInvalid(() -> Validator.contact("12345")));
        check("empty text rejected", throwsInvalid(() -> Validator.text("Name", "   ")));
        check("pipe character rejected", throwsInvalid(() -> Validator.text("Name", "a|b")));
        check("overloaded text() with max length", throwsInvalid(() -> Validator.text("Name", "abcdef", 3)));
        check("Category.fromString is case-insensitive", Category.fromString("eLeCtRoNiCs") == Category.ELECTRONICS);
        check("unknown category rejected", throwsInvalid(() -> Category.fromString("Spaceship")));

        System.out.println("== JSON ==");
        Map<String, String> m = Json.parseObject("{\"name\":\"Kiran \\\"K\\\" Das\",\"n\":5}");
        check("JSON parse keeps escaped quotes", m.get("name").equals("Kiran \"K\" Das") && m.get("n").equals("5"));
        check("JSON quote escapes", Json.quote("a\"b").equals("\"a\\\"b\""));

        System.out.println("== OOP: inheritance, polymorphism, equals ==");
        Item a = lost("L1", "Black Wallet", Category.ACCESSORIES, "black leather wallet", "Library", "2026-10-20");
        Item b = found("F1", "Wallet", Category.ACCESSORIES, "black wallet", "Library", "2026-10-21");
        Item[] both = { a, b };
        check("polymorphic getType()", both[0].getType().equals("lost") && both[1].getType().equals("found"));
        check("polymorphic getContactInfo()", both[0].getContactInfo().startsWith("Owner") && both[1].getContactInfo().startsWith("Found"));
        check("constructor overloading defaults status to ACTIVE", a.getStatus() == ItemStatus.ACTIVE);
        check("equals() compares IDs", a.equals(lost("L1", "Other", Category.OTHER, "x", "y", "2026-01-01")));
        check("toJson() works on any Item", a.toJson().contains("\"t\":\"lost\"") && b.toJson().contains("\"t\":\"found\""));
        check("toCsv() has 10 fields", a.toCsv().split("\\|", -1).length == 10);

        System.out.println("== Matching (interface + inheritance) ==");
        LostItem l1 = lost("L1", "Black Wallet", Category.ACCESSORIES, "black leather wallet with student ID card", "Library", "2026-10-20");
        FoundItem f1 = found("F1", "Wallet", Category.ACCESSORIES, "black leather wallet with id card inside", "Library", "2026-10-21");
        LostItem l3 = lost("L3", "Water Bottle", Category.PERSONAL, "steel silver bottle with sticker", "Hostel Block A", "2026-10-18");
        FoundItem f3 = found("F3", "Umbrella", Category.PERSONAL, "red folding umbrella", "Main Gate", "2026-10-19");
        Matchable weighted = new WeightedMatcher();
        Matchable strict = new StrictMatcher();                    // a child class used through the interface type
        check("wallet pair scores 95", weighted.calculateScore(l1, f1) == 95);
        check("unrelated pair scores 50 (below the 60 threshold)", weighted.calculateScore(l3, f3) == 50);
        check("StrictMatcher gives 0 when location differs", strict.calculateScore(l3, f3) == 0);
        check("StrictMatcher keeps good matches", strict.calculateScore(l1, f1) == 95);
        check("matcher names", weighted.getName().equals("Weighted") && strict.getName().equals("Strict"));

        System.out.println("== Generic repository ==");
        Repository<LostItem> repo = new InMemoryRepository<>("L");
        check("empty repo", repo.isEmpty() && repo.nextId().equals("L1"));
        repo.add(l1); repo.add(l3);
        check("nextId after L1 and L3 is L4", repo.nextId().equals("L4"));
        check("findById is case-insensitive", repo.findById("l3").getName().equals("Water Bottle"));
        boolean missing = false;
        try { repo.findById("X9"); } catch (ItemNotFoundException e) { missing = true; }
        check("unknown ID throws ItemNotFoundException", missing);
        repo.remove("L1");
        check("remove works", repo.findAll().size() == 1);

        System.out.println("== Service end to end (uses a temporary data folder) ==");
        Path tmp = Files.createTempDirectory("findr-test");
        ItemService svc = ServiceFactory.create(tmp.toString());
        check("sample data loaded (8 items)", svc.getAllItems().size() == 8);
        check("3 sample matches found", svc.findMatches().size() == 3);
        check("matches sorted, best first", svc.findMatches().get(0).getScore() >= svc.findMatches().get(1).getScore());
        check("overloaded search(keyword) finds both wallets", svc.search("wallet").size() == 2);
        check("search with criteria (found + canteen)", svc.search(new SearchCriteria("found", "", "canteen", "", "")).size() == 1);

        LostItem umbrella = svc.reportLost("Red Umbrella", "Personal", "red folding umbrella", "Main Gate", "2026-10-19", "Akshat", "8881992292");
        check("new ID is L5", umbrella.getId().equals("L5"));
        List<MatchResult> mr = svc.findMatches(umbrella);
        check("new lost umbrella matches F3 with 100", mr.size() == 1 && mr.get(0).getFound().getId().equals("F3") && mr.get(0).getScore() == 100);
        check("invalid report rejected", throwsInvalid(() -> svc.reportLost("X", "Personal", "d", "l", "bad-date", "n", "9876543210")));

        svc.updateStatus("F3", "claimed");
        check("claimed item leaves the match list", svc.findMatches(umbrella).isEmpty());
        check("status updated", svc.findById("F3").getStatus() == ItemStatus.CLAIMED);

        svc.setMatcher(new StrictMatcher());                       // swap algorithm at run time
        check("matcher swapped at run time", svc.getMatcher().getName().equals("Strict"));

        ItemService reloaded = ServiceFactory.create(tmp.toString());
        check("data survives restart (CSV files)", reloaded.getAllItems().size() == 9
                && reloaded.findById("F3").getStatus() == ItemStatus.CLAIMED);

        svc.delete("L5");
        check("delete works", svc.getAllItems().size() == 8);
        boolean notFound = false;
        try { svc.delete("L5"); } catch (ItemNotFoundException e) { notFound = true; } catch (FindrException e) { }
        check("deleting twice throws ItemNotFoundException", notFound);

        System.out.println("== Comparable ==");
        List<MatchResult> list = new ArrayList<>(Arrays.asList(
                new MatchResult(l1, f1, 70), new MatchResult(l1, f1, 95), new MatchResult(l1, f1, 80)));
        Collections.sort(list);
        check("Collections.sort puts highest score first", list.get(0).getScore() == 95 && list.get(2).getScore() == 70);

        System.out.println("\nRESULT: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
