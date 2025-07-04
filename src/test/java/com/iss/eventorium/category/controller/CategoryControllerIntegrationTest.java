package com.iss.eventorium.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.util.MockMvcAuthHelper;
import jakarta.servlet.Filter;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-test")
class CategoryControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvcAuthHelper authHelper;

    @BeforeAll
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
        authHelper = new MockMvcAuthHelper(mockMvc, objectMapper);
    }

    @Test
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L,5L,10L})
    void testGetCategory_shouldReturnValidCategory(Long id) throws Exception {
        mockMvc.perform(authHelper.authorizedGet(ADMIN_EMAIL, "/api/v1/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 11L, 500L})
    void testGetCategory_shouldThrowEntityNotFoundException(Long id) throws Exception {
        mockMvc.perform(authHelper.authorizedGet(ADMIN_EMAIL, "/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.category.provider.CategoryProvider#provideInvalidCategories")
    @Transactional
    void testCreateCategory_invalidRequest_shouldThrowValidationError(CategoryRequestDto request) throws Exception {
        mockMvc.perform(authHelper.authorizedPost(ADMIN_EMAIL, "/api/v1/categories", request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", matchesPattern(".* is mandatory")));
    }

    @Test
    @Transactional
    void testCreateCategory_shouldThrowCategoryAlreadyExistsException() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Logistics", "Description");
        mockMvc.perform(authHelper.authorizedPost(ADMIN_EMAIL, "/api/v1/categories", request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category with name Logistics already exists"));
    }


    @Test
    @Transactional
    void testCreateCategory() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Name", "Description");
        mockMvc.perform(authHelper.authorizedPost(ADMIN_EMAIL, "/api/v1/categories", request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @ParameterizedTest
    @CsvSource({
            "1,New Category,New Description",
            "2,Catering,Description",
            "3,New Name,Booking venues for events"
    })
    @Transactional
    void testUpdateCategory(Long id, String name, String description) throws Exception {
        CategoryRequestDto request = new CategoryRequestDto(name, description);
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/{id}", request, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description));
    }

    @Test
    void testUpdateCategory_shouldThrowCategoryAlreadyExistsException() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Catering", "New Description");
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/{id}", request, 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category with name Catering already exists"));
    }

    @Test
    @Transactional
    void testUpdateCategory_invalidCategory_shouldThrowEntityNotFoundException() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("New Category", "New Description");
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/{id}", request, 11L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @Transactional
    void testDeleteCategory_shouldThrowCategoryInUseException() throws Exception {
        mockMvc.perform(authHelper.authorizedDelete(ADMIN_EMAIL, "/api/v1/categories/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Unable to delete category because it is currently associated with an active solution."));
    }

    @Test
    @Transactional
    void testDelete_invalidCategory_shouldThrowEntityNotFoundException() throws Exception {
        mockMvc.perform(authHelper.authorizedDelete(ADMIN_EMAIL, "/api/v1/categories/{id}", 11L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @Transactional
    void testDeleteCategory() throws Exception {
        mockMvc.perform(authHelper.authorizedDelete(ADMIN_EMAIL, "/api/v1/categories/{id}", 3L))
                .andExpect(status().isNoContent());
    }

}
