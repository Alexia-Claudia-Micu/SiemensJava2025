package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /*
        Change:
            - Changed return type from <Item> to <?> to allow returning error messages or objects
            - Removed BindingResult and manual error handling
            - Status code on success is 201 Created instead of 400 Bad Request
    */
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item) {
        Item savedItem = itemService.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    /*
        Change:
            - Changed return type from <Item> to <?> to allow flexible responses
            - Added validation to @RequestBody
            - Corrected status code on update to 200 OK instead of 201 Created
            - Return 404 Not Found with a message if item doesn't exist
    */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            Item updatedItem = itemService.save(item);
            return new ResponseEntity<>(updatedItem, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
        }
    }

    /*
        Change:
            - Changed return type from <Void> to <?> to allow returning custom messages
            - Added check if item exists before delete
            - Return 204 No Content on successful delete
            - Return 404 Not Found if the item doesn't exist
    */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        Optional<Item> item = itemService.findById(id);
        if (item.isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>("Item deleted successfully", HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
        }
    }

    /*
    Change:
        - The service method processItemsAsync() was refactored to return CompletableFuture<List<Item>> for proper async behavior.
        - Updated the controller to call .get() on the CompletableFuture to block and retrieve the processed result.
        - This ensures that the controller still returns a synchronous ResponseEntity<List<Item>> to the client.
        - Added try-catch to handle any potential exceptions during async processing and return 500 Internal Server Error if needed.
    */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            List<Item> result = itemService.processItemsAsync().get();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
