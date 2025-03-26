package com.iss.eventorium.solution.service;

import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.category.services.CategoryProposalService;
import com.iss.eventorium.category.services.CategoryService;
import com.iss.eventorium.company.repositories.CompanyRepository;
import com.iss.eventorium.event.models.EventType;
import com.iss.eventorium.event.repositories.EventTypeRepository;
import com.iss.eventorium.event.services.EventService;
import com.iss.eventorium.shared.exceptions.ImageNotFoundException;
import com.iss.eventorium.shared.models.ImagePath;
import com.iss.eventorium.shared.models.Status;
import com.iss.eventorium.solution.dtos.services.CreateServiceRequestDto;
import com.iss.eventorium.solution.dtos.services.ServiceResponseDto;
import com.iss.eventorium.solution.dtos.services.UpdateServiceRequestDto;
import com.iss.eventorium.solution.exceptions.ServiceAlreadyReservedException;
import com.iss.eventorium.solution.mappers.ServiceMapper;
import com.iss.eventorium.solution.models.Service;
import com.iss.eventorium.solution.repositories.ReservationRepository;
import com.iss.eventorium.solution.repositories.ServiceRepository;
import com.iss.eventorium.solution.services.HistoryService;
import com.iss.eventorium.solution.services.ServiceService;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.services.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServiceServiceTest {

    @MockBean
    private ServiceRepository repository;
    @MockBean
    private AuthService authService;
    @MockBean
    private EventService eventService;
    @MockBean
    private HistoryService historyService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private CategoryProposalService categoryProposalService;
    @MockBean
    private CompanyRepository companyRepository;
    @MockBean
    private EventTypeRepository eventTypeRepository;
    @MockBean
    private ReservationRepository reservationRepository;
    @MockBean
    private ServiceMapper mapper;

    @Autowired
    private ServiceService service;
    @Autowired
    private ServiceRepository serviceRepository;


    @Test
    void testDeleteService() {
        Service mockService = mock(Service.class);
        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.of(mockService));
        when(reservationRepository.existsByServiceId(anyLong())).thenReturn(false);

        service.deleteService(1L);

        verify(mockService).setIsDeleted(true);
        verify(repository).save(mockService);
    }

    @Test
    void testDeleteService_invalidService_shouldThrowEntityNotFoundException() {
        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteService(1L));
    }

    @Test
    void testDeleteService_reservedService_shouldThrowServiceAlreadyReservedExceptionException() {
        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.of(new Service()));
        when(reservationRepository.existsByServiceId(anyLong())).thenReturn(true);

        assertThrows(ServiceAlreadyReservedException.class, () -> service.deleteService(1L));
    }

    @Test
    void testUpdateService() {
        UpdateServiceRequestDto request = mock(UpdateServiceRequestDto.class);
        Service mockService = mock(Service.class);

        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.of(new Service()));
        when(eventTypeRepository.findAllById(any())).thenReturn(List.of(new EventType()));
        when(request.getEventTypesIds()).thenReturn(List.of(1L));
        when(mapper.fromUpdateRequest(eq(request), any(Service.class))).thenReturn(mockService);
        when(mapper.toResponse(mockService)).thenReturn(new ServiceResponseDto());

        service.updateService(1L, request);

        verify(historyService).addServiceMemento(mockService);
        verify(mapper).toResponse(mockService);
    }

    @Test
    void testUpdateService_invalidService_shouldThrowEntityNotFoundException() {
        UpdateServiceRequestDto request = new UpdateServiceRequestDto();

        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateService(1L, request));
    }

    @Test
    void testUpdateService_invalidEventType_shouldThrowEntityNotFoundException() {
        UpdateServiceRequestDto request = mock(UpdateServiceRequestDto.class);

        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.of(new Service()));
        when(eventTypeRepository.findAllById(any())).thenReturn(List.of(new EventType()));
        when(request.getEventTypesIds()).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> service.updateService(1L, request));
    }

    @Test
    void testGetImagePath() {
        Service mockService = mock(Service.class);
        ImagePath imagePath = new ImagePath();

        when(mockService.getImagePaths()).thenReturn(List.of(imagePath));
        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.of(mockService));

        ImagePath result = service.getImagePath(1L);

        Assertions.assertEquals(imagePath, result);
    }

    @Test
    void testGetImagePath_noImages_shouldThrowImageNotFoundException() {
        Service mockService = mock(Service.class);

        when(mockService.getImagePaths()).thenReturn(List.of());
        when(repository.findOne((Specification<Service>) any())).thenReturn(Optional.of(mockService));

        assertThrows(ImageNotFoundException.class, () -> service.getImagePath(1L));
    }

    @Test
    void testCreateService_existingCategory() {
        Service mockService = mock(Service.class);
        Category mockCategory = mock(Category.class);
        User provider = new User();

        when(authService.getCurrentUser()).thenReturn(provider);
        when(mockService.getCategory()).thenReturn(mockCategory);
        when(mockCategory.getId()).thenReturn(1L);
        when(mapper.fromCreateRequest(any())).thenReturn(mockService);
        when(mapper.toResponse(mockService)).thenReturn(new ServiceResponseDto());
        when(categoryService.find(anyLong())).thenReturn(mockCategory);

        service.createService(new CreateServiceRequestDto());

        verify(mockService).setStatus(Status.ACCEPTED);
        verify(mockService).setProvider(provider);
        verify(historyService).addServiceMemento(mockService);
        verify(repository).save(mockService);
        verify(mapper).toResponse(mockService);
    }

    @Test
    void testCreateService_suggestedCategory() {
        Service mockService = mock(Service.class);
        Category mockCategory = mock(Category.class);

        User provider = new User();

        when(mapper.fromCreateRequest(any())).thenReturn(mockService);
        when(mockService.getCategory()).thenReturn(mockCategory);
        when(authService.getCurrentUser()).thenReturn(provider);
        when(mockCategory.getId()).thenReturn(null);

        service.createService(new CreateServiceRequestDto());

        verify(categoryProposalService).handleCategoryProposal(mockCategory);
        verify(mockService).setStatus(Status.PENDING);
        verify(mockService).setProvider(provider);
        verify(historyService).addServiceMemento(mockService);
        verify(repository).save(mockService);
        verify(mapper).toResponse(mockService);
    }

    @Test
    void testCreateService_invalidCategory_shouldThrowEntityNotFoundException() {
        Service mockService = mock(Service.class);
        Category mockCategory = mock(Category.class);

        CreateServiceRequestDto request = new CreateServiceRequestDto();

        when(mapper.fromCreateRequest(any())).thenReturn(mockService);
        when(mockService.getCategory()).thenReturn(mockCategory);
        when(mockCategory.getId()).thenReturn(1L);
        when(categoryService.find(mockCategory.getId())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> service.createService(request));
    }


}
