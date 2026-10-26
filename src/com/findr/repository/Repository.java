package com.findr.repository;

import com.findr.exception.ItemNotFoundException;
import com.findr.model.Item;
import java.util.List;

/**
 * GENERIC INTERFACE: Repository<T extends Item> works for LostItem, FoundItem or any other Item type.
 * "extends Item" is a BOUNDED type: T must be an Item or one of its children.
 */
public interface Repository<T extends Item> {
    void add(T item);
    T findById(String id) throws ItemNotFoundException;
    List<T> findAll();
    void remove(String id) throws ItemNotFoundException;
    String nextId();
    boolean isEmpty();
}
