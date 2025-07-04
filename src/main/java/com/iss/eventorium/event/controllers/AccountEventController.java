package com.iss.eventorium.event.controllers;

import com.iss.eventorium.event.api.AccountEventApi;
import com.iss.eventorium.event.dtos.event.CalendarEventDto;
import com.iss.eventorium.event.dtos.event.EventSummaryResponseDto;
import com.iss.eventorium.event.services.AccountEventService;
import com.iss.eventorium.shared.models.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account/events")
@RequiredArgsConstructor
public class AccountEventController implements AccountEventApi {

    private final AccountEventService service;

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarEventDto>> getOrganizerEvents() {
        return ResponseEntity.ok(service.getOrganizerEvents());
    }

    @GetMapping("/my-attending-events")
    public ResponseEntity<List<CalendarEventDto>> getAttendingEvents() {
        return ResponseEntity.ok(service.getAttendingEvents());
    }

    @PostMapping("/{id}/attendance")
    public ResponseEntity<Void> markAttendance(@PathVariable Long id) {
        service.markAttendance(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/rating-eligibility")
    public ResponseEntity<Boolean> getRatingEligibility(@PathVariable Long id) {
        return ResponseEntity.ok(service.isUserEligibleToRate(id));
    }

    @GetMapping("/favourites")
    public ResponseEntity<List<EventSummaryResponseDto>> getFavouriteEvents() {
        return ResponseEntity.ok(service.getFavouriteEvents());
    }

    @PostMapping("/favourites/{id}")
    public ResponseEntity<Void> addFavouriteEvent(@PathVariable Long id) {
        service.addFavouriteEvent(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/favourites/{id}")
    public ResponseEntity<Void> removeFavouriteEvent(@PathVariable Long id) {
        service.removeFavouriteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favourites/{id}")
    public ResponseEntity<Boolean> isFavouriteEvent(@PathVariable Long id) {
        return ResponseEntity.ok(service.isFavouriteEvent(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventSummaryResponseDto>> getAllEvents() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventSummaryResponseDto>> getEvents(Pageable pageable) {
        return ResponseEntity.ok(service.getEventsPaged(pageable));
    }

    @GetMapping("search/all")
    public ResponseEntity<List<EventSummaryResponseDto>> searchEvents(@RequestParam (required = false) String keyword) {
        return ResponseEntity.ok().body(service.searchEvents(keyword));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<EventSummaryResponseDto>> searchEventsPaged(
            @RequestParam (required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(service.searchEvents(keyword, pageable));
    }
}
