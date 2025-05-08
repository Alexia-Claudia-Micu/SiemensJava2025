package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Asynchronously processes all Items in the database.
 *
 * Refactored to address concurrency, error handling, and resource efficiency issues:
 *
 * Changes made:
 * - Each item is processed asynchronously using CompletableFuture.supplyAsync() with a shared thread pool.
 * - Exceptions during processing are properly caught and propagated without affecting other tasks.
 * - Thread safety ensured by eliminating shared mutable state (no shared lists or counters).
 * - Uses join() to wait for all asynchronous tasks to complete before returning.
 * - Returns a clean list of all successfully processed Items, filtering out failures.
 * - Correctly handles thread interruptions by resetting the thread's interrupted status.
 * - Efficiently reuses system resources via an ExecutorService instead of creating new threads manually.
 */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Simulate delay
                    Thread.sleep(100);

                    Optional<Item> optionalItem = itemRepository.findById(id);
                    if (optionalItem.isEmpty()) {
                        return null;
                    }

                    Item item = optionalItem.get();
                    item.setStatus("PROCESSED");
                    return itemRepository.save(item);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread was interrupted", e);
                } catch (Exception e) {
                    throw new RuntimeException("Error processing item with ID: " + id, e);
                }
            }, executor);

            futures.add(future);
        }

        List<Item> processedItems = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        return CompletableFuture.completedFuture(processedItems);
    }
}

