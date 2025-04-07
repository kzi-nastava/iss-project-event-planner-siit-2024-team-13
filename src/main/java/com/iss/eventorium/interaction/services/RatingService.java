package com.iss.eventorium.interaction.services;

import com.iss.eventorium.event.models.Event;
import com.iss.eventorium.event.services.EventService;
import com.iss.eventorium.interaction.dtos.ratings.CreateRatingRequestDto;
import com.iss.eventorium.interaction.dtos.ratings.RatingResponseDto;
import com.iss.eventorium.interaction.mappers.RatingMapper;
import com.iss.eventorium.interaction.models.Rating;
import com.iss.eventorium.interaction.repositories.RatingRepository;
import com.iss.eventorium.notification.models.Notification;
import com.iss.eventorium.notification.models.NotificationType;
import com.iss.eventorium.notification.services.NotificationService;
import com.iss.eventorium.shared.models.CommentableEntity;
import com.iss.eventorium.solution.models.Solution;
import com.iss.eventorium.solution.services.SolutionService;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.services.AuthService;
import jakarta.persistence.EntityNotFoundException;
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

    private final RatingRepository ratingRepository;

    private final RatingMapper mapper;

    private final MessageSource messageSource;

    public RatingResponseDto createSolutionRating(Long solutionId, CreateRatingRequestDto request) {
        Rating rating = mapper.fromCreateRequest(request);
        User user = authService.getCurrentUser();
        rating.setUser(user);

        Solution solution = solutionService.find(solutionId);
        solutionService.addRating(solution, rating);

        sendNotification(solution, user, rating.getRating());
        return mapper.toResponse(rating);
    }

    public RatingResponseDto createEventRating(Long eventId, CreateRatingRequestDto request) {
        Rating rating = mapper.fromCreateRequest(request);
        User user = authService.getCurrentUser();
        rating.setUser(user);

        Event event = eventService.find(eventId);
        eventService.addRating(event, rating);

        sendNotification(event, user, rating.getRating());
        return mapper.toResponse(rating);
    }

    public Rating find(Long id) {
        return ratingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Rating not found")
        );
    }

    private void sendNotification(CommentableEntity entity, User user, Integer rating) {
        notificationService.sendNotification(entity.getCreator(), new Notification(
                "Rating",
                getMessage(user, entity, rating),
                NotificationType.INFO
        ));
    }


    private String getMessage(User user, CommentableEntity entity, Integer rating) {
        String person = user.getPerson().getName() + " " + user.getPerson().getLastname();
        return messageSource.getMessage(
                "notification.rating",
                new Object[] { entity.getDisplayName(), rating, person },
                Locale.getDefault()
        );
    }

}
