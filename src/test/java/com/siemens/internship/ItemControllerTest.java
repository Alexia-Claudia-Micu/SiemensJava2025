package com.siemens.internship;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Test: Create an item with valid data and expect 201 Created
    @Test
    void createItem_withValidData_returnsCreated() throws Exception {
        Item item = new Item(null, "Test Name", "Test Description", "NEW", "test@example.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    // Test: Try to create an item with invalid email and expect 400 Bad Request
    @Test
    void createItem_withInvalidEmail_returnsBadRequest() throws Exception {
        Item item = new Item(null, "Test Name", "Test Description", "NEW", "invalid-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }

    // Test: Try to create an item with blank name and expect 400 Bad Request
    @Test
    void createItem_withBlankName_returnsBadRequest() throws Exception {
        Item item = new Item(null, "", "Valid Description", "NEW", "valid@example.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must not be blank"));
    }

    // Test: Try to create an item with too long description and expect 400 Bad Request
    @Test
    void createItem_withTooLongDescription_returnsBadRequest() throws Exception {
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 260; i++) {
            longDescription.append("a");
        }

        Item item = new Item(null, "Valid Name", longDescription.toString(), "NEW", "valid@example.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description must be smaller than 255 characters"));
    }

    // Test: Get all items and expect 200 OK
    @Test
    void getAllItems_returnsOk() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    // Test: Create and then retrieve an item by ID, expect 200 OK
    @Test
    void getItemById_whenExists_returnsOk() throws Exception {
        Item item = new Item(null, "FindMe", "Desc", "NEW", "findme@example.com");
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("FindMe"));
    }

    // Test: Try to get a non-existing item by ID and expect 204 No Content
    @Test
    void getItemById_whenNotExists_returnsNoContent() throws Exception {
        mockMvc.perform(get("/api/items/999999"))
                .andExpect(status().isNoContent());
    }

    // Test: Update an existing item and expect 200 OK with updated name
    @Test
    void updateItem_whenExists_returnsOk() throws Exception {
        Item item = new Item(null, "UpdateMe", "Before", "NEW", "update@example.com");
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        item.setName("Updated Name");

        mockMvc.perform(put("/api/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    // Test: Try to update a non-existing item and expect 404 Not Found
    @Test
    void updateItem_whenNotExists_returnsNotFound() throws Exception {
        Item item = new Item(null, "NoOne", "None", "NEW", "noone@example.com");

        mockMvc.perform(put("/api/items/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    // Test: Try to update an item with blank name and expect 400 Bad Request
    @Test
    void updateItem_withBlankName_returnsBadRequest() throws Exception {
        Item item = new Item(null, "BeforeUpdate", "Some Description", "NEW", "before@example.com");
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        item.setName("");

        mockMvc.perform(put("/api/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must not be blank"));
    }

    // Test: Try to update an item with too long description and expect 400 Bad Request
    @Test
    void updateItem_withTooLongDescription_returnsBadRequest() throws Exception {
        Item item = new Item(null, "BeforeUpdate", "Valid Description", "NEW", "before@example.com");
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 260; i++) {
            longDescription.append("a");
        }

        item.setDescription(longDescription.toString());

        mockMvc.perform(put("/api/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description must be smaller than 255 characters"));
    }

    // Test: Try to update an item with invalid email and expect 400 Bad Request
    @Test
    void updateItem_withInvalidEmail_returnsBadRequest() throws Exception {
        Item item = new Item(null, "BeforeUpdate", "Valid Description", "NEW", "before@example.com");
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        item.setEmail("invalid-email");

        mockMvc.perform(put("/api/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }

    // Test: Delete an existing item and expect 204 No Content
    @Test
    void deleteItem_whenExists_returnsNoContent() throws Exception {
        Item item = new Item(null, "DeleteMe", "Desc", "NEW", "delete@example.com");
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/items/" + id))
                .andExpect(status().isNoContent());
    }

    // Test: Try to delete a non-existing item and expect 404 Not Found
    @Test
    void deleteItem_whenNotExists_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/items/999999"))
                .andExpect(status().isNotFound());
    }
}