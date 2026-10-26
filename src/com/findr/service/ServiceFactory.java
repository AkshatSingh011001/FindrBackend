package com.findr.service;

import com.findr.exception.StorageException;
import com.findr.matching.WeightedMatcher;
import com.findr.model.*;
import com.findr.repository.*;

/** FACTORY: builds a fully wired ItemService, so Main and ServerMain do not repeat the setup. */
public final class ServiceFactory {
    private ServiceFactory() { }

    public static ItemService create(String dataFolder) throws StorageException {
        FileStorage storage = new FileStorage(dataFolder);
        Repository<LostItem> lostRepo = new InMemoryRepository<>("L");
        Repository<FoundItem> foundRepo = new InMemoryRepository<>("F");
        for (LostItem i : storage.loadLost()) lostRepo.add(i);          // load saved data
        for (FoundItem i : storage.loadFound()) foundRepo.add(i);

        ItemService service = new ItemService(lostRepo, foundRepo, storage, new WeightedMatcher());
        if (service.isEmpty()) SampleData.load(service);                // first run: add sample items
        return service;
    }
}
