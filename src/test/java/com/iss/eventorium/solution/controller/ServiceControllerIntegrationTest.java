package com.iss.eventorium.solution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iss.eventorium.category.dtos.CategoryResponseDto;
import com.iss.eventorium.solution.dtos.services.CreateServiceRequestDto;
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

import static com.iss.eventorium.util.EntityFactory.createServiceRequest;
import static com.iss.eventorium.util.TestUtil.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
class ServiceControllerIntegrationTest {

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
    @Transactional
    void testCreateService_existingCategory_shouldCreateWithStatusAccepted() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        CreateServiceRequestDto request = createServiceRequest(VALID_CATEGORY);

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.category.id").value(request.getCategory().getId()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.price").value(request.getPrice()))
                .andExpect(jsonPath("$.discount").value(request.getDiscount()))
                .andExpect(jsonPath("$.reservationDeadline").value(request.getReservationDeadline()))
                .andExpect(jsonPath("$.cancellationDeadline").value(request.getCancellationDeadline()))
                .andExpect(jsonPath("$.maxDuration").value(request.getMaxDuration()))
                .andExpect(jsonPath("$.minDuration").value(request.getMinDuration()))
                .andExpect(jsonPath("$.available").value(request.getIsAvailable()))
                .andExpect(jsonPath("$.visible").value(request.getIsVisible()))
                .andExpect(jsonPath("$.category.id").value(request.getCategory().getId()));
    }

    @Test
    @Transactional
    void testCreateService_notExistingCategory_shouldCreateWithStatusPending() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        CategoryResponseDto category = new CategoryResponseDto(null, "New Category", "New Category Description");
        CreateServiceRequestDto request = createServiceRequest(category);

        mockMvc.perform(post("/api/v1/services")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.price").value(request.getPrice()))
                .andExpect(jsonPath("$.discount").value(request.getDiscount()))
                .andExpect(jsonPath("$.reservationDeadline").value(request.getReservationDeadline()))
                .andExpect(jsonPath("$.cancellationDeadline").value(request.getCancellationDeadline()))
                .andExpect(jsonPath("$.maxDuration").value(request.getMaxDuration()))
                .andExpect(jsonPath("$.minDuration").value(request.getMinDuration()))
                .andExpect(jsonPath("$.available").value(request.getIsAvailable()))
                .andExpect(jsonPath("$.visible").value(request.getIsVisible()));
    }

    @Test
    @Transactional
    void testCreateService_invalidCategory_shouldThrowEntityNotFound() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        CategoryResponseDto category = CategoryResponseDto.builder().id(500L).build();
        CreateServiceRequestDto request = createServiceRequest(category);

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.solution.provider.ServiceProvider#provideInvalidRequest")
    @Transactional
    void testCreateService_invalidRequest_shouldThrowValidationError(CreateServiceRequestDto request) throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedEndpoints")
    @Transactional
    void testUnauthorizedAccess(HttpMethod method, String url) throws Exception {
        mockMvc.perform(request(method, url))
                .andExpect(status().isUnauthorized());
    }


    private static Stream<Arguments> unauthorizedEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.POST, "/api/v1/services"),
                Arguments.of(HttpMethod.PUT, "/api/v1/services/10"),
                Arguments.of(HttpMethod.DELETE, "/api/v1/services/10")
        );
    }

}
