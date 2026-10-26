package com.findr.repository;

import com.findr.exception.ItemNotFoundException;
import com.findr.model.Item;
import java.util.*;

/** Stores items in memory: an ArrayList (keeps order) plus a HashMap (fast lookup by ID). */
public class InMemoryRepository<T extends Item> implements Repository<T> {
    private final List<T> items = new ArrayList<>();
    private final Map<String, T> index = new HashMap<>();
    private final String prefix;                 // "L" for lost items, "F" for found items

    public InMemoryRepository(String prefix) { this.prefix = prefix; }

    @Override public void add(T item) {
        if (index.containsKey(item.getId())) return;       // ignore duplicates
        items.add(item);
        index.put(item.getId(), item);
    }

    @Override public T findById(String id) throws ItemNotFoundException {
        T item = (id == null) ? null : index.get(id.trim().toUpperCase());
        if (item == null) throw new ItemNotFoundException(id);
        return item;
    }

    /** Returns a COPY so outside code cannot change the internal list (encapsulation). */
    @Override public List<T> findAll() { return new ArrayList<>(items); }

    @Override public void remove(String id) throws ItemNotFoundException {
        T item = findById(id);
        items.remove(item);
        index.remove(item.getId());
    }

    /** L1, L2, L3... : one more than the biggest number used so far. */
    @Override public String nextId() {
        int max = 0;
        for (T item : items) {
            try { max = Math.max(max, Integer.parseInt(item.getId().substring(prefix.length()))); }
            catch (NumberFormatException ignored) { }
        }
        return prefix + (max + 1);
    }

    @Override public boolean isEmpty() { return items.isEmpty(); }
}
