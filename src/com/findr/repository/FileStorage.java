package com.findr.repository;

import com.findr.exception.FindrException;
import com.findr.exception.StorageException;
import com.findr.model.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/** FILE HANDLING: saves items to data/lost.csv and data/found.csv and loads them back. */
public class FileStorage {
    private final Path folder;
    private final Path lostFile;
    private final Path foundFile;

    public FileStorage(String folderName) {
        this.folder = Paths.get(folderName);
        this.lostFile = folder.resolve("lost.csv");
        this.foundFile = folder.resolve("found.csv");
    }

    /** "? extends Item" (wildcard generics): accepts a List<LostItem> or a List<FoundItem>. */
    public void save(List<? extends Item> lost, List<? extends Item> found) throws StorageException {
        write(lostFile, lost);
        write(foundFile, found);
    }

    private void write(Path file, List<? extends Item> items) throws StorageException {
        try {
            Files.createDirectories(folder);
            try (BufferedWriter w = Files.newBufferedWriter(file)) {      // try-with-resources closes the file
                for (Item item : items) { w.write(item.toCsv()); w.newLine(); }
            }
        } catch (IOException e) {
            throw new StorageException("Could not save " + file + ": " + e.getMessage());
        }
    }

    public List<LostItem> loadLost() throws StorageException {
        List<LostItem> list = new ArrayList<>();
        for (String[] p : read(lostFile)) {
            try {
                list.add(new LostItem(p[1], p[2], Category.fromString(p[3]), p[4], p[5],
                        LocalDate.parse(p[6]), ItemStatus.fromString(p[7]), p[8], p[9]));
            } catch (FindrException | DateTimeParseException e) { /* skip a corrupted line */ }
        }
        return list;
    }

    public List<FoundItem> loadFound() throws StorageException {
        List<FoundItem> list = new ArrayList<>();
        for (String[] p : read(foundFile)) {
            try {
                list.add(new FoundItem(p[1], p[2], Category.fromString(p[3]), p[4], p[5],
                        LocalDate.parse(p[6]), ItemStatus.fromString(p[7]), p[8], p[9]));
            } catch (FindrException | DateTimeParseException e) { /* skip a corrupted line */ }
        }
        return list;
    }

    private List<String[]> read(Path file) throws StorageException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) return rows;                     // first run: no file yet
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length == 10) rows.add(parts);
            }
        } catch (IOException e) {
            throw new StorageException("Could not read " + file + ": " + e.getMessage());
        }
        return rows;
    }
}
