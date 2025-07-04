package com.iss.eventorium.interaction.services;

import com.iss.eventorium.event.models.Event;
import com.iss.eventorium.event.services.EventService;
import com.iss.eventorium.interaction.dtos.ratings.CreateRatingRequestDto;
import com.iss.eventorium.interaction.dtos.ratings.RatingResponseDto;
import com.iss.eventorium.interaction.exceptions.AlreadyRatedException;
import com.iss.eventorium.interaction.mappers.RatingMapper;
import com.iss.eventorium.interaction.models.Rating;
import com.iss.eventorium.notifications.models.Notification;
import com.iss.eventorium.notifications.models.NotificationType;
import com.iss.eventorium.notifications.services.NotificationService;
import com.iss.eventorium.solution.models.Solution;
import com.iss.eventorium.solution.services.SolutionService;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final AuthService authService;
    private final EventService eventService;
    private final SolutionService solutionService;
    private final NotificationService notificationService;

    private final RatingMapper mapper;

    private final MessageSource messageSource;

    public RatingResponseDto createSolutionRating(Long solutionId, CreateRatingRequestDto request) {
        Rating rating = mapper.fromCreateRequest(request);
        User user = authService.getCurrentUser();
        rating.setRater(user);

        Solution solution = solutionService.find(solutionId);
        checkIfAlreadyRated(solution, user);
        solutionService.addRating(solution, rating);

        sendNotification(solution.getProvider(), solution.getName(), user, rating.getRating());
        return mapper.toResponse(rating);
    }

    public RatingResponseDto createEventRating(Long eventId, CreateRatingRequestDto request) {
        Rating rating = mapper.fromCreateRequest(request);
        User rater = authService.getCurrentUser();
        rating.setRater(rater);

        Event event = eventService.find(eventId);
        checkIfAlreadyRated(event, rater);
        eventService.addRating(event, rating);

        sendNotification(event.getOrganizer(), event.getName(), rater, rating.getRating());
        return mapper.toResponse(rating);
    }

    private void sendNotification(User objectCreator, String displayName, User rater, Integer rating) { // Object creator is one who created product, service or event
        notificationService.sendNotification(objectCreator, new Notification(
                "Rating",
                getMessage(rater, displayName, rating),
                NotificationType.INFO
        ));
    }

    private String getMessage(User user, String displayName, Integer rating) {
        String person = user.getPerson().getName() + " " + user.getPerson().getLastname();
        return messageSource.getMessage(
                "notification.rating",
                new Object[] { displayName, rating, person },
                Locale.getDefault()
        );
    }

    public void checkIfAlreadyRated(Solution solution, User user) {
        for (Rating r : solution.getRatings()) {
            if (r.getRater().equals(user)) {
                throw new AlreadyRatedException("Solution is already rated");
            }
        }
    }

    public void checkIfAlreadyRated(Event event, User user) {
        for (Rating r : event.getRatings()) {
            if (r.getRater().equals(user)) {
                throw new AlreadyRatedException("Event is already rated");
            }
        }
    }
}