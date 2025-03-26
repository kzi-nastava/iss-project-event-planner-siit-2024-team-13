package com.iss.eventorium.solution.provider;


import com.iss.eventorium.solution.dtos.services.CreateServiceRequestDto;
import com.iss.eventorium.solution.dtos.services.ServiceFilterDto;
import com.iss.eventorium.solution.dtos.services.UpdateServiceRequestDto;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static com.iss.eventorium.util.EntityFactory.createServiceRequest;
import static com.iss.eventorium.util.TestUtil.VALID_CATEGORY;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ServiceProvider {

    public static Stream<Arguments> provideInvalidCreateRequest() {
        CreateServiceRequestDto invalidMaxDuration = createServiceRequest(VALID_CATEGORY);
        invalidMaxDuration.setMaxDuration(50);

        CreateServiceRequestDto invalidMinDuration = createServiceRequest(VALID_CATEGORY);
        invalidMinDuration.setMinDuration(-10);

        CreateServiceRequestDto invalidPrice = createServiceRequest(VALID_CATEGORY);
        invalidPrice.setPrice(-10.0);

        CreateServiceRequestDto invalidDiscount = createServiceRequest(VALID_CATEGORY);
        invalidDiscount.setDiscount(101.0);

        CreateServiceRequestDto invalidDeadLines = createServiceRequest(VALID_CATEGORY);
        invalidDeadLines.setReservationDeadline(-1);
        invalidDeadLines.setCancellationDeadline(-1);

        return Stream.of(
                arguments(new CreateServiceRequestDto()),
                arguments(createServiceRequest(null)),
                arguments(invalidDeadLines),
                arguments(invalidDiscount),
                arguments(invalidPrice),
                arguments(invalidMaxDuration),
                arguments(invalidMinDuration)
        );
    }

    public static Stream<Arguments> provideInvalidUpdateRequest() {
        UpdateServiceRequestDto invalidMaxDuration = createServiceRequest();
        invalidMaxDuration.setMaxDuration(50);

        UpdateServiceRequestDto invalidMinDuration = createServiceRequest();
        invalidMinDuration.setMinDuration(-10);

        UpdateServiceRequestDto invalidPrice = createServiceRequest();
        invalidPrice.setPrice(-10.0);

        UpdateServiceRequestDto invalidDiscount = createServiceRequest();
        invalidDiscount.setDiscount(101.0);

        UpdateServiceRequestDto invalidDeadLines = createServiceRequest();
        invalidDeadLines.setReservationDeadline(-1);
        invalidDeadLines.setCancellationDeadline(-1);

        return Stream.of(
                arguments(new UpdateServiceRequestDto()),
                arguments(invalidDeadLines),
                arguments(invalidDiscount),
                arguments(invalidPrice),
                arguments(invalidMaxDuration),
                arguments(invalidMinDuration)
        );
    }

    public static Stream<Arguments> provideServiceFilterCases() {
        return Stream.of(
                arguments(new ServiceFilterDto("Event", null, null, null, null, null, null), 2),
                arguments(new ServiceFilterDto("Photography", null, null, null, null, null, null), 1),
                arguments(new ServiceFilterDto("Catering", null, null, null, null, null, null), 1),
                arguments(new ServiceFilterDto("Planning", null, null, null, null, null, null), 1),
                arguments(new ServiceFilterDto("Transportation", null, null, null, null, null, null), 0),
                arguments(new ServiceFilterDto("Service", null, null, null, null, null, null), 2),
                arguments(new ServiceFilterDto("Invalid", null, null, null, null, null, null), 1),
                arguments(new ServiceFilterDto(null, null, "Birthday Party", null, null, null, null), 3),
                arguments(new ServiceFilterDto(null, null, null, "Photography", null, null, null), 1),
                arguments(new ServiceFilterDto(null, null, null, "Catering", null, null, null), 1),
                arguments(new ServiceFilterDto(null, null, null, "Event Planning", null, null, null), 1),
                arguments(new ServiceFilterDto(null, null, null, null, true, null, null), 4),
                arguments(new ServiceFilterDto(null, null, null, null, null, 100.0, 200.0), 2),
                arguments(new ServiceFilterDto(null, null, null, null, null, 500.0, 1500.0), 1),
                arguments(new ServiceFilterDto(null, null, null, null, null, 300.0, 400.0), 0)
        );
    }

}
