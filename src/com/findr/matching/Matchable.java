package com.findr.matching;

import com.findr.model.FoundItem;
import com.findr.model.LostItem;

/**
 * INTERFACE (also the STRATEGY pattern): a contract for "how to score a lost/found pair".
 * ItemService depends on this interface, not on a particular class, so the algorithm can be swapped.
 */
public interface Matchable {
    int calculateScore(LostItem lost, FoundItem found);
    String getName();
}
