package com.iss.eventorium.category.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.category.dtos.UpdateStatusRequestDto;
import com.iss.eventorium.util.MockMvcAuthHelper;
import jakarta.servlet.Filter;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-test")
class CategoryProposalControllerIntegrationTest {

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
    void testGetPendingCategories() throws Exception {
        mockMvc.perform(authHelper.authorizedGet(ADMIN_EMAIL, "/api/v1/categories/pending/all"))
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
        mockMvc.perform(authHelper.authorizedPatch(ADMIN_EMAIL, "/api/v1/categories/pending/{id}", request, categoryId))
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
        mockMvc.perform(authHelper.authorizedPatch(ADMIN_EMAIL, "/api/v1/categories/pending/{id}", request, categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedMessage));

    }

    @Test
    @Transactional
    void testUpdateCategoryStatus_invalidRequest_shouldThrowValidationError() throws Exception {
        UpdateStatusRequestDto request = new UpdateStatusRequestDto(null);
        mockMvc.perform(authHelper.authorizedPatch(ADMIN_EMAIL, "/api/v1/categories/pending/{id}", request, 10L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", matchesPattern(".* is mandatory")));
    }

    @Test
    @Transactional
    void testUpdateCategory() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Marketing", "New Category Description");
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/pending/{id}", request, 10L))
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
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/pending/{id}", request, categoryId))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @Transactional
    void testChangeCategory() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Guest Management", "Handling guest invitations and RSVP");
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/pending/{id}/change", request, 10L))
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
        mockMvc.perform(authHelper.authorizedPut(ADMIN_EMAIL, "/api/v1/categories/pending/{id}/change", request, categoryId))
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
