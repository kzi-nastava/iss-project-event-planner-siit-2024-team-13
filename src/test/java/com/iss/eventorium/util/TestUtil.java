package com.iss.eventorium.util;

import org.springframework.jdbc.core.JdbcTemplate;

public class TestUtil {

    public static final String RESERVATION_ENDPOINT = "/api/v1/events/{event-id}/services/{service-id}/reservation";

    public static final String ORGANIZER_EMAIL = "organizer@gmail.com";
    public static final String ORGANIZER_EMAIL_2 = "organizer2@gmail.com";
    public static final String PASSWORD = "pera";
    public static final Long NON_EXISTENT_ENTITY_ID = 0L;
    public static final Long EXISTING_EVENT = 1L;
    public static final Long INVALID_EVENT = 0L;
    public static final Long INVALID_PRODUCT = 0L;
    public static final Long NEW_BUDGET_ITEM = 4L;
    public static final Long PURCHASED_PRODUCT = 1L;
    public static final Long NOT_PROCESSED_PRODUCT = 5L;
    public static final Long DELETED_PRODUCT = 6L;
    public static final Long INVISIBLE_PRODUCT = 7L;
    public static final Long UNAVAILABLE_PRODUCT = 8L;
    public static final Long EVENT_WITHOUT_AGENDA_1 = 1L;
    public static final Long EVENT_WITHOUT_AGENDA_2 = 7L;
    public static final Long EVENT_WITH_AGENDA = 6L;
    public static final Long EVENT_WITH_GUESTS = 6L;
    public static final Long EVENT_WITHOUT_GUESTS = 1L;
    public static final Long EVENT_NOT_IN_DRAFT_STATE = 5L;
    public static final Long FORBIDDEN_EVENT_ID = 2L;
    public static final Long EVENT_ID_NOT_OWNED_BY_LOGGED_IN_ORGANIZER = 3L;
    public static final Long VALID_EVENT_ID_FOR_RESERVATION_1 = 8L;
    public static final Long VALID_EVENT_ID_FOR_RESERVATION_2 = 5L;
    public static final Long EVENT_IN_PAST_ID = 9L;
    public static final Long NON_EXISTING_ENTITY_ID = 0L;
    public static final Long PLANNED_BUDGET_ITEM = 8L;
    public static final Long ORGANIZER_2_EVENT = 2L;
    public static final Long PROCESSED_BUDGET_ITEM = 1L;
    public static final Long PLANNED_BUDGET_ITEM_2 = 9L;
    public static final Long ORGANIZER_2_BUDGET_ITEM = 3L;
    public static final Long EMPTY_BUDGET_EVENT = 7L;
    public static final Long PURCHASABLE_PRODUCT = 3L;
    public static final Long DELETED_SERVICE = 24L;
    public static final Long INVISIBLE_SERVICE = 25L;

    public static final Long UNAVAILABLE_SERVICE_ID = 12L;
    public static final Long SERVICE_ID_RESERVATION_DEADLINE_EXPIRED = 13L; // Refers to a VALID_EVENT_ID_FOR_RESERVATION
    public static final Long RESERVABLE_SERVICE_ID_1 = 9L;
    public static final Long SERVICE_ID_WITH_DURATION_RANGE_1 = 14L;
    public static final Long SERVICE_ID_WITH_DURATION_RANGE_2 = 18L;
    public static final Long SERVICE_ID_WITH_FIXED_DURATION = 15L;
    public static final Long RESERVABLE_SERVICE_ID_2 = 16L;
    public static final Long OVERLAPPING_SERVICE_ID = 17L;
    public static final Long SERVICE_WITHOUT_DISCOUNT = 19L;
    public static final Long SERVICE_WITH_DISCOUNT = 20L;

    private static void resetTable(JdbcTemplate jdbcTemplate, String tableName) {
        jdbcTemplate.execute(String.format("DELETE FROM %s;", tableName));
        jdbcTemplate.execute(String.format("ALTER TABLE %s ALTER COLUMN id RESTART WITH 1;", tableName));
    }

    public static void resetTables(JdbcTemplate jdbcTemplate) {
        resetTable(jdbcTemplate, "roles");
        resetTable(jdbcTemplate, "cities");
        resetTable(jdbcTemplate, "users");
        resetTable(jdbcTemplate, "event_types");
        resetTable(jdbcTemplate, "events");
    }

}
