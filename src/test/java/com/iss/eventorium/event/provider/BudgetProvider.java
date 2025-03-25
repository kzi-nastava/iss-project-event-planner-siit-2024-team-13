package com.iss.eventorium.event.provider;

import com.iss.eventorium.category.dtos.CategoryResponseDto;
import com.iss.eventorium.event.dtos.budget.BudgetItemRequestDto;
import com.iss.eventorium.event.models.Budget;
import com.iss.eventorium.event.models.BudgetItem;
import com.iss.eventorium.event.models.Event;
import com.iss.eventorium.solution.models.Product;
import com.iss.eventorium.solution.models.Solution;
import com.iss.eventorium.solution.models.SolutionType;
import com.iss.eventorium.user.dtos.auth.LoginRequestDto;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.iss.eventorium.util.TestUtil.VALID_CATEGORY;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BudgetProvider {

    public static Stream<Arguments> provideBudgetItems() {
        return Stream.of(
                arguments(new LoginRequestDto("organizer@gmail.com", "pera"), 3),
                arguments(new LoginRequestDto("organizer2@gmail.com", "pera"), 2),
                arguments(new LoginRequestDto("organizer3@gmail.com", "pera"), 1)
        );
    }

    public static Stream<Arguments> provideInvalidBudgetItems() {
        return Stream.of(
                arguments(BudgetItemRequestDto.builder().build()),
                arguments(
                    BudgetItemRequestDto.builder()
                            .itemId(1L)
                            .category(VALID_CATEGORY)
                            .plannedAmount(-50.0)
                            .itemType(SolutionType.PRODUCT)
                            .build()
                ),
                arguments(
                        BudgetItemRequestDto.builder()
                                .category(VALID_CATEGORY)
                                .plannedAmount(1000.0)
                                .itemType(SolutionType.PRODUCT)
                                .build()
                ),
                arguments(
                        BudgetItemRequestDto.builder()
                                .itemId(1L)
                                .category(VALID_CATEGORY)
                                .itemType(SolutionType.PRODUCT)
                                .build()
                ),
                arguments(
                        BudgetItemRequestDto.builder()
                                .itemId(1L)
                                .plannedAmount(1000.0)
                                .itemType(SolutionType.PRODUCT)
                                .build()
                )
        );
    }

    public static Stream<Arguments> provideEventsForBudget() {
        Solution solution1 = Product.builder().id(1L).build();
        Solution solution2 = Product.builder().id(2L).build();
        Solution solutionDuplicate = Product.builder().id(1L).build();

        BudgetItem item1 = BudgetItem.builder().solution(solution1).build();
        BudgetItem item2 = BudgetItem.builder().solution(solution2).build();
        BudgetItem itemDuplicate = BudgetItem.builder().solution(solutionDuplicate).build();

        Budget budget1 = new Budget();
        budget1.setItems(List.of(item1, itemDuplicate));
        Budget budget2 = new Budget();
        budget2.setItems(List.of(item2));

        Event event1 = new Event();
        event1.setBudget(budget1);
        Event event2 = new Event();
        event2.setBudget(budget2);
        Event eventWithoutBudget = new Event();

        return Stream.of(
                arguments(Collections.emptyList(), 0),
                arguments(List.of(eventWithoutBudget), 0),
                arguments(List.of(event1), 1),
                arguments(List.of(event1, event2), 2)
        );
    }

}
