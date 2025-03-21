package com.iss.eventorium.category.provider;

import com.iss.eventorium.category.dtos.CategoryRequestDto;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class CategoryProvider {

    public static Stream<Arguments> provideInvalidCategories() {
        return Stream.of(
                Arguments.of(new CategoryRequestDto()),
                Arguments.of(new CategoryRequestDto("Name", null)),
                Arguments.of(new CategoryRequestDto(null, "Description"))
        );
    }
}
