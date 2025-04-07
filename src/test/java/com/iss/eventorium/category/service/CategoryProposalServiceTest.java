package com.iss.eventorium.category.service;

import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.category.dtos.CategoryResponseDto;
import com.iss.eventorium.category.exceptions.CategoryAlreadyExistsException;
import com.iss.eventorium.category.mappers.CategoryMapper;
import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.category.repositories.CategoryRepository;
import com.iss.eventorium.category.services.CategoryProposalService;
import com.iss.eventorium.category.services.CategoryService;
import com.iss.eventorium.notification.models.Notification;
import com.iss.eventorium.notification.services.NotificationService;
import com.iss.eventorium.shared.models.Status;
import com.iss.eventorium.solution.models.Product;
import com.iss.eventorium.solution.models.Solution;
import com.iss.eventorium.solution.services.SolutionService;
import com.iss.eventorium.user.models.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CategoryProposalServiceTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private SolutionService solutionService;
    @MockBean
    private CategoryRepository categoryRepository;
    @MockBean
    private CategoryMapper mapper;

    @Autowired
    private CategoryProposalService service;

    private Solution solution;
    private Category category;
    private User provider;
    private CategoryRequestDto request;
    private CategoryResponseDto response;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setSuggested(true);

        provider = new User();
        provider.setId(2L);

        solution = new Product();
        solution.setId(3L);
        solution.setCategory(category);
        solution.setStatus(Status.PENDING);
        solution.setProvider(provider);

        request = new CategoryRequestDto();
        request.setName("Updated Category");
        request.setDescription("Updated Description");

        response = new CategoryResponseDto();
    }

    @Test
    void testUpdateCategoryStatus_shouldAcceptCategory() {
        when(categoryService.find(1L)).thenReturn(category);
        when(solutionService.findSolutionByCategory(category)).thenReturn(solution);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(response);

        CategoryResponseDto result = service.updateCategoryStatus(1L, Status.ACCEPTED);

        assertFalse(category.isSuggested());
        verify(notificationService).sendNotification(eq(provider), any(Notification.class));
        assertEquals(response, result);
    }

    @Test
    void testUpdateCategoryStatus_shouldDeclineCategory() {
        when(categoryService.find(1L)).thenReturn(category);
        when(solutionService.findSolutionByCategory(category)).thenReturn(solution);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(response);

        CategoryResponseDto result = service.updateCategoryStatus(1L, Status.DECLINED);

        assertTrue(category.isDeleted());
        assertTrue(solution.getIsDeleted());
        verify(notificationService).sendNotification(eq(provider), any(Notification.class));
        assertEquals(response, result);
    }

    @Test
    void testChangeCategoryProposal() {
        when(categoryService.find(1L)).thenReturn(category);
        when(solutionService.findSolutionByCategory(category)).thenReturn(solution);
        when(categoryService.findByName(request.getName())).thenReturn(new Category());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(mapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponseDto result = service.changeCategoryProposal(1L, request);

        assertTrue(category.isDeleted());
        verify(notificationService).sendNotification(eq(provider), any(Notification.class));
        assertEquals(response, result);
    }

    @Test
    void testUpdateCategoryStatus_shouldThrowEntityNotFoundException() {
        category.setSuggested(false);
        when(categoryService.find(1L)).thenReturn(category);

        assertThrows(EntityNotFoundException.class, () -> service.updateCategoryStatus(1L, Status.ACCEPTED));
    }

    @Test
    void testUpdateCategoryProposal_shouldThrowCategoryAlreadyExistsException() {
        when(categoryService.find(1L)).thenReturn(category);
        when(categoryService.checkCategoryExistence(category, request.getName())).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> service.updateCategoryProposal(1L, request));
    }

    @Test
    void testUpdateCategoryProposal() {
        when(categoryService.find(1L)).thenReturn(category);
        when(categoryService.checkCategoryExistence(category, request.getName())).thenReturn(false);
        when(solutionService.findSolutionByCategory(category)).thenReturn(solution);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(response);

        CategoryResponseDto result = service.updateCategoryProposal(1L, request);

        assertFalse(category.isSuggested());
        assertEquals("Updated Category", category.getName());
        assertEquals(response, result);
        verify(notificationService).sendNotification(eq(provider), any(Notification.class));
    }

    @Test
    void testGetPendingCategories_shouldReturnList() {
        when(categoryRepository.findBySuggestedTrue()).thenReturn(List.of(category));
        when(mapper.toResponse(category)).thenReturn(response);

        List<CategoryResponseDto> result = service.getPendingCategories();

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
    }

    @Test
    void testGetPendingCategories_shouldReturnEmptyList() {
        when(categoryRepository.findBySuggestedTrue()).thenReturn(List.of());

        List<CategoryResponseDto> result = service.getPendingCategories();

        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleCategoryProposal_shouldMarkSuggestedAndNotifyAdmin() {
        doNothing().when(notificationService).sendNotificationToAdmin(any(Notification.class));

        service.handleCategoryProposal(category);

        assertTrue(category.isSuggested());
        verify(notificationService).sendNotificationToAdmin(any(Notification.class));
    }

    @Test
    void testHandleCategoryProposal_shouldThrowExceptionForDuplicateName() {
        doThrow(new CategoryAlreadyExistsException("Category already exists"))
                .when(categoryService).ensureCategoryNameIsUnique(category);

        assertThrows(CategoryAlreadyExistsException.class, () ->
                service.handleCategoryProposal(category));
    }


}
