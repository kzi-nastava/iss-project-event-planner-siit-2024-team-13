package com.iss.eventorium.solution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iss.eventorium.category.dtos.CategoryResponseDto;
import com.iss.eventorium.solution.dtos.services.CreateServiceRequestDto;
import com.iss.eventorium.solution.dtos.services.ServiceFilterDto;
import jakarta.servlet.Filter;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
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

    @Test
    @Transactional
    void testDeleteService() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(delete("/api/v1/services/{id}", VALID_SERVICE_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    void testDeleteService_hasReservation_shouldThrowServiceAlreadyReserved() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(delete("/api/v1/services/{id}", SERVICE_WITH_RESERVATION_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("The service cannot be deleted because it is currently reserved."));
    }

    @Test
    @Transactional
    void testDeleteService_invalidService_shouldThrowEntityNotFound() throws Exception {
        String token = login(mockMvc, objectMapper, PROVIDER_LOGIN);
        mockMvc.perform(delete("/api/v1/services/{id}", INVALID_SERVICE_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Service not found"));
    }

    @Test
    void testGetService() throws Exception {
        mockMvc.perform(get("/api/v1/services/{id}", 7))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(VALID_SERVICE_ID))
                .andExpect(jsonPath("$.name").value("Event Planning"))
                .andExpect(jsonPath("$.description").value("Comprehensive event planning services from start to finish"))
                .andExpect(jsonPath("$.provider.id").value(3L));
    }

    @Test
    void testGetService_invalidService_shouldThrowEntityNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/services/{id}", INVALID_SERVICE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Service not found"));
    }

    @Test
    void testGetAllServices() throws Exception {
        mockMvc.perform(get("/api/v1/services/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data/service_search_test.csv")
    void testSearchService(String keyword, int expected) throws Exception {
        mockMvc.perform(get("/api/v1/services/search/all?keyword={keyword}", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expected));
    }

    @ParameterizedTest
    @MethodSource("com.iss.eventorium.solution.provider.ServiceProvider#provideServiceFilterCases")
    void testFilterServices(ServiceFilterDto filter, int expected) throws Exception {
        mockMvc.perform(get("/api/v1/services/filter/all")
                .param("name", filter.getName())
                .param("description", filter.getDescription())
                .param("type", filter.getType())
                .param("category", filter.getCategory())
                .param("availability", filter.getAvailability() != null ? filter.getAvailability().toString() : null)
                .param("minPrice", filter.getMinPrice() != null ? filter.getMinPrice().toString() : null)
                .param("maxPrice", filter.getMaxPrice() != null ? filter.getMaxPrice().toString() : null))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(expected));
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
                Arguments.of(HttpMethod.PUT, "/api/v1/services/7"),
                Arguments.of(HttpMethod.DELETE, "/api/v1/services/7")
        );
    }
}
