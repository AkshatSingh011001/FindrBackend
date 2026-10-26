package com.findr.service;

import com.findr.exception.StorageException;
import com.findr.model.*;
import java.time.LocalDate;

/** Sample items used the first time the program runs. */
public final class SampleData {
    private SampleData() { }

    public static void load(ItemService s) throws StorageException {
        s.add(new LostItem("L1", "Black Wallet", Category.ACCESSORIES, "black leather wallet with student ID card", "Library", LocalDate.parse("2026-10-20"), "Aarav Sharma", "9876543210"));
        s.add(new LostItem("L2", "iPhone 13", Category.ELECTRONICS, "blue iphone with cracked back cover", "Canteen", LocalDate.parse("2026-10-21"), "Ananya Verma", "ananya@college.edu"));
        s.add(new LostItem("L3", "Water Bottle", Category.PERSONAL, "steel silver bottle with sticker", "Hostel Block A", LocalDate.parse("2026-10-18"), "Rahul Meena", "9812345678"));
        s.add(new LostItem("L4", "Calculator", Category.STATIONERY, "casio scientific calculator black", "Exam Hall 2", LocalDate.parse("2026-10-22"), "Priya Singh", "9898989898"));
        s.add(new FoundItem("F1", "Wallet", Category.ACCESSORIES, "black leather wallet with id card inside", "Library", LocalDate.parse("2026-10-21"), "Security Desk", "Security Office"));
        s.add(new FoundItem("F2", "Mobile Phone", Category.ELECTRONICS, "blue iphone cracked cover", "Canteen", LocalDate.parse("2026-10-22"), "Kiran Das", "Admin Block"));
        s.add(new FoundItem("F3", "Umbrella", Category.PERSONAL, "red folding umbrella", "Main Gate", LocalDate.parse("2026-10-19"), "Security Desk", "Security Office"));
        s.add(new FoundItem("F4", "Scientific Calculator", Category.STATIONERY, "casio calculator black colour", "Exam Hall 2", LocalDate.parse("2026-10-23"), "Invigilator", "Exam Cell"));
    }
}
