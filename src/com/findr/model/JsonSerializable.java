package com.findr.model;

/** INTERFACE: any object that can describe itself as JSON for the website (Item and MatchResult both do). */
public interface JsonSerializable {
    String toJson();
}
