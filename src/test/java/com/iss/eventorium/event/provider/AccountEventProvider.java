package com.iss.eventorium.event.provider;

import com.iss.eventorium.user.dtos.auth.LoginRequestDto;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AccountEventProvider {

    public static Stream<Arguments> provideOrganizerEvents() {
        return Stream.of(
                arguments(new LoginRequestDto("organizer@gmail.com", "pera"), 2),
                arguments(new LoginRequestDto("organizer2@gmail.com", "pera"), 2),
                arguments(new LoginRequestDto("organizernoevents@gmail.com", "pera"), 0)
        );
    }
}
