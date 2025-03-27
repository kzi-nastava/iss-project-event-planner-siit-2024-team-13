package com.iss.eventorium.solution.service;

import com.iss.eventorium.solution.dtos.services.ServiceResponseDto;
import com.iss.eventorium.solution.mappers.ServiceMapper;
import com.iss.eventorium.solution.models.Service;
import com.iss.eventorium.solution.repositories.ServiceRepository;
import com.iss.eventorium.solution.services.AccountServiceService;
import com.iss.eventorium.user.models.Person;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.repositories.UserRepository;
import com.iss.eventorium.user.services.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountServiceServiceTest {

    @MockBean
    private ServiceRepository repository;
    @MockBean
    private AuthService authService;
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ServiceMapper mapper;

    @Autowired
    private AccountServiceService service;


    @Test
    void testAddFavouriteService() {
        User user = new User();
        Person mockPerson = mock(Person.class);
        user.setPerson(mockPerson);
        Service mockService = mock(Service.class);

        List<Service> favouriteServices = new ArrayList<>();
        favouriteServices.add(new Service());

        when(mapper.toResponse(mockService)).thenReturn(new ServiceResponseDto());
        when(mockPerson.getFavouriteServices()).thenReturn(favouriteServices);
        when(repository.findById(anyLong())).thenReturn(Optional.of(mockService));
        when(authService.getCurrentUser()).thenReturn(user);

        ServiceResponseDto result = service.addFavouriteService(1L);

        verify(userRepository).save(user);
        verify(mapper).toResponse(mockService);
        assertNotNull(result);
    }

    @Test
    void testAddFavouriteService_alreadyFavourite() {
        User user = new User();
        Person mockPerson = mock(Person.class);
        user.setPerson(mockPerson);
        Service mockService = new Service();

        List<Service> favouriteServices = new ArrayList<>();
        favouriteServices.add(mockService);

        when(mapper.toResponse(mockService)).thenReturn(new ServiceResponseDto());
        when(mockPerson.getFavouriteServices()).thenReturn(favouriteServices);
        when(repository.findById(anyLong())).thenReturn(Optional.of(mockService));
        when(authService.getCurrentUser()).thenReturn(user);

        ServiceResponseDto result = service.addFavouriteService(1L);

        verifyNoInteractions(userRepository);
        verify(mapper).toResponse(mockService);
        assertNotNull(result);
    }

    @Test
    void testAddService_invalidService_shouldThrowEntityNotFoundException() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addFavouriteService(1L));
    }


}
