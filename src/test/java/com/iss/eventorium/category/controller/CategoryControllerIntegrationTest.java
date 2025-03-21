package com.iss.eventorium.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iss.eventorium.category.dtos.CategoryRequestDto;
import jakarta.servlet.Filter;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.iss.eventorium.util.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
class CategoryControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L,5L,10L})
    void testGetCategory_shouldReturnValidCategory(Long id) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(get("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 11L, 500L})
    void testGetCategory_shouldThrowEntityNotFoundException(Long id) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(get("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.category.provider.CategoryProvider#provideInvalidCategories")
    @Transactional
    void testCreateCategory_invalidRequest_shouldThrowValidationError(CategoryRequestDto request) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", matchesPattern(".* is mandatory")));
    }

    @Test
    @Transactional
    void testCreateCategory_shouldThrowCategoryAlreadyExistsException() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        CategoryRequestDto request = new CategoryRequestDto("Logistics", "Description");
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category with name Logistics already exists"));
    }


    @Test
    @Transactional
    void testCreateCategory() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        CategoryRequestDto request = new CategoryRequestDto("Name", "Description");
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    @Transactional
    void testUpdateCategory() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        CategoryRequestDto request = new CategoryRequestDto("New Category", "New Description");
        mockMvc.perform(put("/api/v1/categories/{id}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.description").value("New Description"));
    }

    @Test
    @Transactional
    void testUpdateCategory_invalidCategory_shouldThrowEntityNotFoundException() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        CategoryRequestDto request = new CategoryRequestDto("New Category", "New Description");
        mockMvc.perform(put("/api/v1/categories/{id}", 11L)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @Transactional
    void testDeleteCategory_shouldThrowCategoryInUseException() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(delete("/api/v1/categories/{id}", 1L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Unable to delete category because it is currently associated with an active solution."));
    }

    @Test
    @Transactional
    void testDelete_invalidCategory_shouldThrowEntityNotFoundException() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(delete("/api/v1/categories/{id}", 11L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @Transactional
    void testDeleteCategory() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(delete("/api/v1/categories/{id}", 3L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

}
