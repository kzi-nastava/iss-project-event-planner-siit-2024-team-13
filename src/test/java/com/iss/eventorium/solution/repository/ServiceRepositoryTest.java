package com.iss.eventorium.solution.repository;

import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.event.models.EventType;
import com.iss.eventorium.solution.models.Service;
import com.iss.eventorium.solution.repositories.ServiceRepository;
import com.iss.eventorium.solution.specifications.ServiceSpecification;
import com.iss.eventorium.user.models.Role;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.models.UserBlock;
import com.iss.eventorium.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.iss.eventorium.util.EntityFactory.*;
import static com.iss.eventorium.util.TestUtil.resetTables;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Slf4j
class ServiceRepositoryTest {

    @Autowired
    private ServiceRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        resetTables(jdbcTemplate);

        Role provider = new Role();
        provider.setName("PROVIDER");
        entityManager.persist(provider);

        EventType eventType = createEventType("Wedding");
        entityManager.persist(eventType);

        Category category = createCategory("Name", "Description", false);
        entityManager.persist(category);

        User provider1 = createUser("provider@gmail.com", "1", provider);
        User provider2 = createUser("provider1@gmail.com", "2", provider);
        User provider3 = createUser("provider2@gmail.com", "3", provider);
        entityManager.persist(provider1);
        entityManager.persist(provider2);
        entityManager.persist(provider3);


        entityManager.persist(createService("Service1", provider1, eventType, category));
        entityManager.persist(createService("Service2", provider1, eventType, category));
        entityManager.persist(createService("Service3", provider1, eventType, category));
        entityManager.persist(createService("Service4", provider2, eventType, category));
        entityManager.persist(createService("Service5", provider2, eventType, category));
        Service deletedService = createService("Service6", provider2, eventType, category);
        deletedService.setIsDeleted(true);
        entityManager.persist(deletedService);
    }

    @ParameterizedTest
    @CsvSource({
            "1,3",
            "2,2",
            "3,0"
    })
    void testFilterByProvider(Long providerId, int expected) {
        User provider = entityManager.find(User.class, providerId);

        List<Service> result = repository.findAll(ServiceSpecification.filterForProvider(provider));

        assertNotNull(result);
        assertEquals(expected, result.size());
    }

    @ParameterizedTest
    @CsvSource({
            "Service1,1",
            "InvalidService,0",
            "SeRvIcE2,1",
            "Ser   ,3",
            "   Service3    ,1",
            "SERVICE2, 1"
    })
    void testFilterByNameForProvider(String name, int expected) {
        User user = entityManager.find(User.class, 1L);

        List<Service> result = repository.findAll(ServiceSpecification.filterByNameForProvider(name, user));

        assertNotNull(result);
        assertEquals(expected, result.size());
    }

    @Test
    void testFilter_withBlockedUser() {
        User user1 = entityManager.find(User.class, 1L);
        User user2 = entityManager.find(User.class, 2L);

        UserBlock userBlock = new UserBlock(user1, user2);
        entityManager.persist(userBlock);

        List<Service> result = repository.findAll(ServiceSpecification.filter(user1));

        assertNotNull(result);
        assertEquals(3, result.size());
    }


}
