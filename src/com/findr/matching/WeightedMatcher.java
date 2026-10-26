package com.findr.matching;

import com.findr.model.FoundItem;
import com.findr.model.LostItem;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Default algorithm. Score out of 100:
 *   same category 30 | same location 20 | date within 2 days 20 (3-7 days: 10) | description words up to 30
 */
public class WeightedMatcher implements Matchable {
    // static final = constants shared by all objects
    protected static final int CATEGORY_POINTS = 30;
    protected static final int LOCATION_POINTS = 20;
    protected static final int DATE_POINTS = 20;
    protected static final int DESCRIPTION_POINTS = 30;

    // COLLECTIONS: HashSet of filler words that are ignored when comparing descriptions
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "with", "and", "of", "in", "on", "at", "to", "has", "have", "inside", "it"));

    @Override
    public int calculateScore(LostItem lost, FoundItem found) {
        int score = 0;
        if (lost.getCategory() == found.getCategory()) score += CATEGORY_POINTS;               // enums compare with ==
        if (lost.getLocation().equalsIgnoreCase(found.getLocation())) score += LOCATION_POINTS;

        long gap = Math.abs(ChronoUnit.DAYS.between(lost.getDate(), found.getDate()));
        if (gap <= 2) score += DATE_POINTS; else if (gap <= 7) score += DATE_POINTS / 2;

        Set<String> a = keywords(lost.getDescription() + " " + lost.getName());
        Set<String> b = keywords(found.getDescription() + " " + found.getName());
        Set<String> common = new HashSet<>(a); common.retainAll(b);         // words in both
        Set<String> all = new HashSet<>(a); all.addAll(b);                  // words in either
        if (!all.isEmpty()) score += (int) Math.round((double) DESCRIPTION_POINTS * common.size() / all.size());
        return score;
    }

    @Override public String getName() { return "Weighted"; }

    /** STRING HANDLING: lowercase, split into words, drop stop words. */
    private Set<String> keywords(String text) {
        Set<String> words = new HashSet<>();
        for (String w : text.toLowerCase().split("[^a-z0-9]+"))
            if (!w.isEmpty() && !STOP_WORDS.contains(w)) words.add(w);
        return words;
    }
}
