package com.findr.model;

import com.findr.util.Json;

/**
 * An IMMUTABLE class (final fields, no setters): once a match is created it never changes.
 * COMPOSITION: a MatchResult has-a LostItem, has-a FoundItem and a score.
 * INTERFACES: Comparable (so Collections.sort works) and JsonSerializable.
 */
public final class MatchResult implements Comparable<MatchResult>, JsonSerializable {
    private final LostItem lost;
    private final FoundItem found;
    private final int score;

    public MatchResult(LostItem lost, FoundItem found, int score) {
        this.lost = lost; this.found = found; this.score = score;
    }

    public LostItem getLost() { return lost; }
    public FoundItem getFound() { return found; }
    public int getScore() { return score; }

    /** Natural ordering: HIGHEST score first. */
    @Override public int compareTo(MatchResult other) { return Integer.compare(other.score, this.score); }

    @Override public String toJson() {
        return "{\"l\":" + Json.quote(lost.getId()) + ",\"f\":" + Json.quote(found.getId()) + ",\"s\":" + score + "}";
    }

    public void display() {
        System.out.println("MATCH SCORE: " + score + "/100");
        System.out.println("  LOST : " + lost.getId() + " " + lost.getName() + " (" + lost.getLocation() + ", " + lost.getDate() + ")");
        System.out.println("         " + lost.getContactInfo());
        System.out.println("  FOUND: " + found.getId() + " " + found.getName() + " (" + found.getLocation() + ", " + found.getDate() + ")");
        System.out.println("         " + found.getContactInfo());
        System.out.println("--------------------------------------------");
    }
}
