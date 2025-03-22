package com.iss.eventorium.category.provider;

import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.category.dtos.UpdateStatusRequestDto;
import com.iss.eventorium.shared.models.Status;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

public class CategoryProposalProvider {

    private static final CategoryRequestDto VALID_CATEGORY_REQUEST = new CategoryRequestDto("Guest Management", "Handling guest invitations and RSVP");
    private static final CategoryRequestDto VALID_NEW_CATEGORY_REQUEST = new CategoryRequestDto("New Name", "New Description");

    public static Stream<Arguments> provideUpdateCategoryProposal() {
        return Stream.of(
                Arguments.of(8L, new UpdateStatusRequestDto(Status.ACCEPTED)),
                Arguments.of(10L, new UpdateStatusRequestDto(Status.DECLINED))
        );
    }

    public static Stream<Arguments> provideInvalidUpdateCategoryStatus() {
        return Stream.of(
            Arguments.of(1L, new UpdateStatusRequestDto(Status.ACCEPTED), "Category is not suggested"),
            Arguments.of(12L, new UpdateStatusRequestDto(Status.DECLINED), "Solution with category 'Not Found' not found"),
            Arguments.of(13L, new UpdateStatusRequestDto(Status.DECLINED), "Solution with category 'Not Pending' is not pending"),
            Arguments.of(500L, new UpdateStatusRequestDto(Status.DECLINED), "Category not found")
        );
    }

    public static Stream<Arguments> provideInvalidChangeCategory() {
        return Stream.of(
            Arguments.of(1L, VALID_CATEGORY_REQUEST, "Category is not suggested"),
            Arguments.of(12L, VALID_CATEGORY_REQUEST, "Solution with category 'Not Found' not found"),
            Arguments.of(500L, VALID_CATEGORY_REQUEST, "Category not found"),
            Arguments.of(13L, VALID_CATEGORY_REQUEST, "Solution with category 'Not Pending' is not pending"),
            Arguments.of(8L, new CategoryRequestDto("Guest", "Handling guest invitations and RSVP"), "Category with name 'Guest' not found")
        );
    }

    public static Stream<Arguments> provideInvalidUpdateCategory() {
        return Stream.of(
            Arguments.of(1L, VALID_NEW_CATEGORY_REQUEST, "Category is not suggested", HttpStatus.NOT_FOUND.value()),
            Arguments.of(12L, VALID_NEW_CATEGORY_REQUEST, "Solution with category 'Not Found' not found", HttpStatus.NOT_FOUND.value()),
            Arguments.of(13L, VALID_NEW_CATEGORY_REQUEST, "Solution with category 'Not Pending' is not pending", HttpStatus.NOT_FOUND.value()),
            Arguments.of(500L, VALID_NEW_CATEGORY_REQUEST, "Category not found", HttpStatus.NOT_FOUND.value()),
            Arguments.of(10L, new CategoryRequestDto("Catering", "New Description"), "Category with name 'Catering' already exists", HttpStatus.CONFLICT.value())
        );
    }

}
