package com.iss.eventorium.solution.provider;


import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.solution.dtos.services.CreateServiceRequestDto;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static com.iss.eventorium.util.EntityFactory.createServiceRequest;
import static com.iss.eventorium.util.TestUtil.VALID_CATEGORY;

public class ServiceProvider {

    public static Stream<Arguments> provideInvalidRequest() {
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
                Arguments.of(new CreateServiceRequestDto()),
                Arguments.of(createServiceRequest(null)),
                Arguments.of(invalidDeadLines),
                Arguments.of(invalidDiscount),
                Arguments.of(invalidPrice),
                Arguments.of(invalidMaxDuration),
                Arguments.of(invalidMinDuration)
        );
    }

}
