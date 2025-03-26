package com.iss.eventorium.solution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static com.iss.eventorium.util.TestUtil.*;
import static com.iss.eventorium.util.TestUtil.INVALID_SERVICE_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
class AccountServiceControllerIntegrationTest {

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

    @ParameterizedTest
    @MethodSource("unauthorizedEndpoints")
    @Transactional
    void testUnauthorizedAccess(HttpMethod method, String url) throws Exception {
        mockMvc.perform(request(method, url)).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetProfileServices() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(get("/api/v1/account/services/all")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetFavouriteServices() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(get("/api/v1/account/services/favourites")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Transactional
    void testAddFavouriteService() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(post("/api/v1/account/services/favourites/{id}", VALID_SERVICE_ID)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(VALID_SERVICE_ID));
    }

    @Test
    @Transactional
    void testAddFavouriteService_invalidService_shouldThrowEntityNotFound() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(post("/api/v1/account/services/favourites/{id}", INVALID_SERVICE_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Service not found"));
    }

    @Test
    @Transactional
    void testRemoveFavouriteService() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(delete("/api/v1/account/services/favourites/{id}", VALID_SERVICE_ID)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    void testRemoveFavouriteService_invalidService_shouldThrowEntityNotFound() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(delete("/api/v1/account/services/favourites/{id}", INVALID_SERVICE_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Service not found"));
    }

    private static Stream<Arguments> unauthorizedEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/account/services/all"),
                Arguments.of(HttpMethod.GET, "/api/v1/account/services/filter/all"),
                Arguments.of(HttpMethod.GET, "/api/v1/account/services/filter"),
                Arguments.of(HttpMethod.GET, "/api/v1/account/services/search/all"),
                Arguments.of(HttpMethod.GET, "/api/v1/account/services/search"),
                Arguments.of(HttpMethod.GET, "/api/v1/account/services/favourites"),
                Arguments.of(HttpMethod.GET, String.format("/api/v1/account/services/favourites/%s", VALID_SERVICE_ID)),
                Arguments.of(HttpMethod.POST, String.format("/api/v1/account/services/favourites/%s", VALID_SERVICE_ID)),
                Arguments.of(HttpMethod.DELETE, String.format("/api/v1/account/services/favourites/%s", VALID_SERVICE_ID))
        );
    }

}
