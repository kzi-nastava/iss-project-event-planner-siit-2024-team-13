package com.iss.eventorium.category.repository;

import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.category.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static com.iss.eventorium.util.EntityFactory.createCategory;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindBySuggestedFalse() {
        loadCategories();
        List<Category> result = repository.findBySuggestedFalse();

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @ParameterizedTest
    @CsvSource({
            "true,0",
            "false,10"
    })
    void testFindBySuggestedFalse_allTheSame(boolean suggested, int expected) {
        for(int i=0; i<expected; i++) {
          entityManager.persist(createCategory("Name" + i, "Desc"+i, suggested));
        }

        List<Category> result = repository.findBySuggestedFalse();

        assertNotNull(result);
        assertEquals(expected, result.size());
    }

    @Test
    void testFindBySuggestedTrue() {
        loadCategories();
        List<Category> result = repository.findBySuggestedTrue();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @ParameterizedTest
    @CsvSource({
            "true,10",
            "false,0"
    })
    void testFindBySuggestedTrue_allTheSame(boolean suggested, int expected) {
        for(int i=0; i<expected; i++) {
            entityManager.persist(createCategory("Name" + i, "Desc"+i, suggested));
        }

        List<Category> result = repository.findBySuggestedTrue();

        assertNotNull(result);
        assertEquals(expected, result.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Category1", "category1", "CATEGORY1",
            "Category1   ", "  Category1", "  Category1   ",
            "Special & Category", "CatEgory1"
    })
    void testFindByNameIgnoreCase(String name) {
        loadCategories();
        Optional<Category> result = repository.findByNameIgnoreCase(name);

        assertNotNull(result);
        assertTrue(result.isPresent());
    }

    @Test
    void testFindByNameIgnoreCase_nonExistent() {
        loadCategories();
        Optional<Category> result = repository.findByNameIgnoreCase("nonexistent");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    private void loadCategories() {
        entityManager.persist(createCategory("Category1", "Desc", true));
        entityManager.persist(createCategory("Category2", "Desc", false));
        entityManager.persist(createCategory("Category3", "Desc", false));
        entityManager.persist(createCategory("Category4", "Desc", false));
        entityManager.persist(createCategory("Category5", "Desc", false));
        entityManager.persist(createCategory("Category6", "Desc", true));
        entityManager.persist(createCategory("Special & Category", "Desc", true));
        Category deletedCategory = createCategory("Deleted Category", "Desc", true);
        deletedCategory.setDeleted(true);
        entityManager.persist(deletedCategory);

    }

}
