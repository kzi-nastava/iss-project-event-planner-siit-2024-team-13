package com.iss.eventorium.util;

import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.category.dtos.CategoryResponseDto;
import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.event.dtos.eventtype.EventTypeResponseDto;
import com.iss.eventorium.event.models.Event;
import com.iss.eventorium.event.models.EventType;
import com.iss.eventorium.event.models.Privacy;
import com.iss.eventorium.shared.models.City;
import com.iss.eventorium.solution.dtos.services.CreateServiceRequestDto;
import com.iss.eventorium.solution.dtos.services.UpdateServiceRequestDto;
import com.iss.eventorium.solution.models.ReservationType;
import com.iss.eventorium.user.models.Person;
import com.iss.eventorium.user.models.Role;
import com.iss.eventorium.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntityFactory {

    public static User createUser(String email, String hash, Role role) {
        return User.builder()
                .person(createPerson())
                .email(email)
                .password("$2a$10$Z3JiBldbaNQ4qGPjtr7TV.FeT2He/KgqxT68impZ9.H3XeyQAZ03W") // pera
                .verified(true)
                .activationTimestamp(new Date(1733572800000L))
                .lastPasswordReset(new Date(1506923938508L))
                .deactivated(false)
                .hash(hash)
                .roles(List.of(role))
                .build();
    }

    public static Event createEvent(String name, String description, int daysFromNow, String address, City city, User organizer, EventType type) {
        return Event.builder()
                .name(name)
                .description(description)
                .date(LocalDate.now().plusDays(daysFromNow))
                .privacy(Privacy.OPEN)
                .maxParticipants(100)
                .type(type)
                .city(city)
                .address(address)
                .activities(new ArrayList<>())
                .organizer(organizer)
                .isDraft(false)
                .build();
    }

    public static Category createCategory(String name, String description, boolean suggested) {
        return Category.builder()
                .name(name)
                .description(description)
                .suggested(suggested)
                .build();
    }

    public static EventType createEventType(String name) {
        return EventType.builder()
                .name(name)
                .description("Event type description")
                .deleted(false)
                .build();
    }

    public static CreateServiceRequestDto createServiceRequest(CategoryResponseDto category) {
        return CreateServiceRequestDto.builder()
                .name("New Service")
                .description("New Description")
                .specialties("New Specialties")
                .eventTypes(List.of(EventTypeResponseDto.builder().id(1L).build()))
                .price(100.0)
                .discount(0.0)
                .category(category)
                .type(ReservationType.AUTOMATIC)
                .reservationDeadline(30)
                .cancellationDeadline(20)
                .minDuration(5)
                .maxDuration(5)
                .isAvailable(true)
                .isVisible(true)
                .build();
    }

    public static UpdateServiceRequestDto createServiceRequest() {
        return UpdateServiceRequestDto.builder()
                .name("Updated Service")
                .description("Updated Description")
                .specialties("Updated Specialties")
                .eventTypesIds(List.of(1L, 2L))
                .price(110.0)
                .discount(10.0)
                .type(ReservationType.MANUAL)
                .reservationDeadline(20)
                .cancellationDeadline(10)
                .minDuration(1)
                .maxDuration(10)
                .available(false)
                .visible(false)
                .build();
    }


    private static Person createPerson() {
        return Person.builder()
                .name("John")
                .lastname("Doe")
                .build();
    }

}
