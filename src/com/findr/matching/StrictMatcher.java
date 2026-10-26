package com.findr.matching;

import com.findr.model.FoundItem;
import com.findr.model.LostItem;

/**
 * INHERITANCE + OVERRIDING: a stricter matcher. It reuses the parent's scoring (super), but
 * if the category or the location differs the pair scores 0 immediately.
 */
public class StrictMatcher extends WeightedMatcher {
    @Override
    public int calculateScore(LostItem lost, FoundItem found) {
        if (lost.getCategory() != found.getCategory()) return 0;
        if (!lost.getLocation().equalsIgnoreCase(found.getLocation())) return 0;
        return super.calculateScore(lost, found);
    }

    @Override public String getName() { return "Strict"; }
}
