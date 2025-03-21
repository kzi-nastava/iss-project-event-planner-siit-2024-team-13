package com.iss.eventorium.category.repositories;

import com.iss.eventorium.category.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findBySuggestedFalse();
    Page<Category> findBySuggestedFalse(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE TRIM(LOWER(c.name)) = LOWER(TRIM(:name))")
    Optional<Category> findByNameIgnoreCase(String name);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE TRIM(LOWER(c.name)) = LOWER(TRIM(:name))")
    boolean existsByNameIgnoreCase(String name);

    List<Category> findBySuggestedTrue();
    Page<Category> findBySuggestedTrue(Pageable pageable);
}


