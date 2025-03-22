package com.iss.eventorium.category.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.category.dtos.UpdateStatusRequestDto;
import jakarta.servlet.Filter;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static com.iss.eventorium.util.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
class CategoryProposalControllerIntegrationTest {

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
    void testGetPendingCategories() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(get("/api/v1/categories/pending/all")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[1].id").value(10))
                .andExpect(jsonPath("$[2].id").value(12))
                .andExpect(jsonPath("$[3].id").value(13));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.category.provider.CategoryProposalProvider#provideUpdateCategoryProposal")
    @Transactional
    void testUpdateCategoryStatus(Long categoryId, UpdateStatusRequestDto request) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(patch("/api/v1/categories/pending/{id}", categoryId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId));
    }


    @ParameterizedTest
    @MethodSource("com.iss.eventorium.category.provider.CategoryProposalProvider#provideInvalidUpdateCategoryStatus")
    @Transactional
    void testUpdateCategoryStatus_invalidRequest_shouldThrow(
            Long categoryId,
            UpdateStatusRequestDto request,
            String expectedMessage
    ) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(patch("/api/v1/categories/pending/{id}", categoryId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedMessage));

    }

    @Test
    @Transactional
    void testUpdateCategoryStatus_invalidRequest_shouldThrowValidationError() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        UpdateStatusRequestDto request = new UpdateStatusRequestDto(null);
        mockMvc.perform(patch("/api/v1/categories/pending/{id}", 10L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", matchesPattern(".* is mandatory")));
    }

    @Test
    @Transactional
    void testUpdateCategory() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        CategoryRequestDto request = new CategoryRequestDto("Marketing", "New Category Description");
        mockMvc.perform(put("/api/v1/categories/pending/{id}", 10L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Marketing"))
                .andExpect(jsonPath("$.description").value("New Category Description"));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.category.provider.CategoryProposalProvider#provideInvalidUpdateCategory")
    @Transactional
    void testUpdateCategory_invalidRequest_shouldThrow(
            Long categoryId,
            CategoryRequestDto request,
            String expectedMessage,
            int expectedStatus
    ) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(put("/api/v1/categories/pending/{id}", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @Transactional
    void testChangeCategory() throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        CategoryRequestDto request = new CategoryRequestDto("Guest Management", "Handling guest invitations and RSVP");
        mockMvc.perform(put("/api/v1/categories/pending/{id}/change", 10L)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.name").value("Guest Management"))
                .andExpect(jsonPath("$.description").value("Handling guest invitations and RSVP"));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.category.provider.CategoryProposalProvider#provideInvalidChangeCategory")
    @Transactional
    void testChangeCategory_invalidRequest_shouldThrow(
            Long categoryId,
            CategoryRequestDto request,
            String expectedMessage
    ) throws Exception {
        String token = login(mockMvc, objectMapper, ADMIN_LOGIN);
        mockMvc.perform(put("/api/v1/categories/pending/{id}/change", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("unauthorizedEndpoints")
    @Transactional
    void testUnauthorizedAccess(HttpMethod method, String url) throws Exception {
        mockMvc.perform(request(method, url)).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> unauthorizedEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/categories/pending/all"),
                Arguments.of(HttpMethod.PATCH, "/api/v1/categories/pending/10"),
                Arguments.of(HttpMethod.PUT, "/api/v1/categories/pending/10/change"),
                Arguments.of(HttpMethod.PUT, "/api/v1/categories/10")
        );
    }

}
