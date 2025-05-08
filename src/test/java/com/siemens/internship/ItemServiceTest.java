package com.siemens.internship;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    // Clean before each test
    @BeforeEach
    void cleanDatabase() {
        itemRepository.deleteAll();
    }

    // Test: Normal processing of multiple items
    @Test
    void processItemsAsync_shouldProcessAllItems() throws Exception {
        // Given
        itemRepository.save(new Item(null, "Item1", "Desc1", "NEW", "item1@example.com"));
        itemRepository.save(new Item(null, "Item2", "Desc2", "NEW", "item2@example.com"));

        // When
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        // Then
        assertThat(processedItems).hasSize(2);
        assertThat(processedItems).allSatisfy(item -> {
            assertThat(item.getStatus()).isEqualTo("PROCESSED");
        });
    }

    // Test: No items in the database
    @Test
    void processItemsAsync_withNoItems_shouldReturnEmptyList() throws Exception {
        // Given: No items in DB

        // When
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        // Then
        assertThat(processedItems).isEmpty();
    }

    // Test: Some items are deleted before processing (simulate disappearing items)
    @Test
    void processItemsAsync_withMissingItem_shouldSkipNull() throws Exception {
        // Given
        Item item = itemRepository.save(new Item(null, "Item1", "Desc1", "NEW", "item1@example.com"));
        itemRepository.save(new Item(null, "Item2", "Desc2", "NEW", "item2@example.com"));

        // Simulate that one item is deleted after getting IDs but before fetching
        itemRepository.deleteById(item.getId());

        // When
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        // Then
        assertThat(processedItems).hasSize(1);
        assertThat(processedItems.get(0).getName()).isEqualTo("Item2");
        assertThat(processedItems.get(0).getStatus()).isEqualTo("PROCESSED");
    }

    // Test: Very slow processing (simulate thread sleep) - should still work
    @Test
    void processItemsAsync_withDelay_shouldStillProcess() throws Exception {
        // Given
        itemRepository.save(new Item(null, "SlowItem", "Slow", "NEW", "slow@example.com"));

        // When
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        // Then
        assertThat(processedItems).hasSize(1);
        assertThat(processedItems.get(0).getStatus()).isEqualTo("PROCESSED");
    }
}