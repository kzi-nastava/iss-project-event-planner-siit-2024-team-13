package com.iss.eventorium.event.services;

import com.iss.eventorium.event.dtos.*;
import com.iss.eventorium.event.mappers.ActivityMapper;
import com.iss.eventorium.event.mappers.EventMapper;
import com.iss.eventorium.event.models.Activity;
import com.iss.eventorium.event.models.Event;
import com.iss.eventorium.event.models.Privacy;
import com.iss.eventorium.event.repositories.EventRepository;
import com.iss.eventorium.event.repositories.EventSpecification;
import com.iss.eventorium.shared.utils.PagedResponse;
import com.iss.eventorium.user.services.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EventService {

    private final EventRepository repository;
    private final AuthService authService;

    public List<EventSummaryResponseDto> getTopEvents() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Event> events = repository.findTopFiveUpcomingEvents(getUserCity(), pageable);
        return events.stream().map(EventMapper::toSummaryResponse).collect(Collectors.toList());
    }

    private String getUserCity(){  // If the user is logged in, it returns the city from the profile, otherwise defaults to "Novi Sad".
        if (authService.getCurrentUser() != null)
            return authService.getCurrentUser().getPerson().getCity().getName();
        return "Novi Sad";
    }

    public List<EventSummaryResponseDto> getAll(){
        List<Event> events = repository.findAll();
        return events.stream().map(EventMapper::toSummaryResponse).collect(Collectors.toList());
    }

    public PagedResponse<EventSummaryResponseDto> searchEvents (String keyword, Pageable pageable) {
        if (keyword.isBlank()) {
            return EventMapper.toPagedResponse(repository.findAll(pageable));
        }
        return EventMapper.toPagedResponse(repository.findByNameContainingAllIgnoreCase(keyword, pageable));
    }

    public PagedResponse<EventSummaryResponseDto> getEventsPaged (Pageable pageable) {
        return EventMapper.toPagedResponse(repository.findAll(pageable));
    }

    public PagedResponse<EventSummaryResponseDto> filterEvents (EventFilterDto filter, Pageable pageable) {
        Specification<Event> specification = EventSpecification.filterBy(filter);
        return EventMapper.toPagedResponse(repository.findAll(specification, pageable));
    }

    public EventResponseDto createEvent(EventRequestDto eventRequestDto)  {
        Event created = repository.save(prepareEvent(eventRequestDto));
        return EventMapper.toResponse(created);
    }

    private Event prepareEvent(EventRequestDto eventRequestDto) {
        Event event = EventMapper.fromRequest(eventRequestDto);
        event.setOrganizer(authService.getCurrentUser());
        return event;
    }

    public void createAgenda(Long id, List<ActivityRequestDto> request) {
        Event event = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Event not found with ID: " + id));

        List<Activity> activities = request.stream()
                .map(ActivityMapper::fromRequest)
                .toList();

        event.getActivities().clear();
        event.getActivities().addAll(activities);

        if (event.getPrivacy().equals(Privacy.OPEN)) event.setDraft(false);
        repository.save(event);
    }

}
