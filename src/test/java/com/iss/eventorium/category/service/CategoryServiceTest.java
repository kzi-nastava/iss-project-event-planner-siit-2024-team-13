package com.iss.eventorium.category.service;

import com.iss.eventorium.category.dtos.CategoryRequestDto;
import com.iss.eventorium.category.dtos.CategoryResponseDto;
import com.iss.eventorium.category.exceptions.CategoryAlreadyExistsException;
import com.iss.eventorium.category.exceptions.CategoryInUseException;
import com.iss.eventorium.category.mappers.CategoryMapper;
import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.category.repositories.CategoryRepository;
import com.iss.eventorium.category.services.CategoryService;
import com.iss.eventorium.solution.services.SolutionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CategoryServiceTest {

    @MockBean
    private SolutionService solutionService;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private CategoryMapper mapper;

    @Autowired
    private CategoryService categoryService;

    private Category category;
    private CategoryRequestDto categoryRequestDto;
    private CategoryResponseDto categoryResponseDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("TestCategory")
                .description("TestDescription")
                .suggested(false)
                .build();

        categoryRequestDto = CategoryRequestDto.builder()
                .name("TestCategory")
                .description("TestDescription")
                .build();

        categoryResponseDto = CategoryResponseDto.builder()
                .id(1L)
                .name("TestCategory")
                .build();
    }

    @Test
    void testGetCategories() {
        when(categoryRepository.findBySuggestedFalse()).thenReturn(List.of(category));
        when(mapper.toResponse(category)).thenReturn(categoryResponseDto);

        List<CategoryResponseDto> result = categoryService.getCategories();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("TestCategory", result.get(0).getName());
    }

    @Test
    void testGetCategory_shouldReturnCategory_whenExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toResponse(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.getCategory(1L);
        assertNotNull(result);
        assertEquals("TestCategory", result.getName());
    }

    @Test
    void testGetCategory_shouldThrowException_whenNotExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategory(1L));
    }

    @Test
    void testCreateCategory_shouldCreateCategory_whenNameIsUnique() {
        when(categoryRepository.existsByNameIgnoreCase("TestCategory")).thenReturn(false);
        when(mapper.fromRequest(categoryRequestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.createCategory(categoryRequestDto);

        assertNotNull(result);
        assertEquals("TestCategory", result.getName());
    }

    @Test
    void testCreateCategory_shouldThrowException_whenNameAlreadyExists() {
        when(categoryRepository.existsByNameIgnoreCase("TestCategory")).thenReturn(true);
        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.createCategory(categoryRequestDto));
    }

    @Test
    void testUpdateCategory_shouldUpdateCategory_whenValid() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase("TestCategory")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.updateCategory(1L, categoryRequestDto);

        assertNotNull(result);
        assertEquals("TestCategory", result.getName());
    }

    @Test
    void testUpdateCategory_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.updateCategory(1L, categoryRequestDto));
    }

    @Test
    void testUpdateCategory_shouldThrowException_whenNameAlreadyExists() {
        Category toUpdate = Category.builder().name("New Category").description("New Description").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(toUpdate));
        when(categoryRepository.existsByNameIgnoreCase(category.getName())).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.updateCategory(1L, categoryRequestDto));
    }

    @Test
    void testDeleteCategory_shouldDelete_whenNotInUse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(solutionService.existsCategory(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).save(category);
        assertTrue(category.isDeleted());
        assertTrue(category.getName().contains("_"));
    }

    @Test
    void testDeleteCategory_shouldThrowException_whenInUse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(solutionService.existsCategory(1L)).thenReturn(true);

        assertThrows(CategoryInUseException.class, () -> categoryService.deleteCategory(1L));
    }

}
