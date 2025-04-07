package com.iss.eventorium.notification.services;

import com.iss.eventorium.notification.repositories.NotificationRepository;
import com.iss.eventorium.notification.dtos.NotificationResponseDto;
import com.iss.eventorium.notification.mappers.NotificationMapper;
import com.iss.eventorium.notification.models.Notification;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AuthService authService;
    private final NotificationRepository repository;

    private final NotificationMapper mapper;

    public void sendNotification(User user, Notification notification) {
        log.info("Sending notification to {}: {}", user.getId(), notification.getMessage());
        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/notifications",
                mapper.toResponse(notification)
        );
        notification.setRecipient(user);
        repository.save(notification);
    }

    public void sendNotificationToAdmin(Notification notification) {
        log.info("Sending notification to admins: {}", notification.getMessage());
        messagingTemplate.convertAndSend(
          "/topic/admin",
          mapper.toResponse(notification)
        );

        repository.save(notification);
    }

    public List<NotificationResponseDto> getAllNotifications() {
        return getNotifications().stream().map(mapper::toResponse).toList();
    }

    private List<Notification> getNotifications() {
        User loggedIn = authService.getCurrentUser();
        List<Notification> notifications;

        if (loggedIn.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName())))
            notifications = repository.findAllByRecipientIsNullOrderByTimestampDesc();
        else
            notifications = repository.findByRecipient_IdOrderByTimestampDesc(loggedIn.getId());

        return notifications;
    }

    public void markAsSeen() {
        List<Notification> notifications = getNotifications();
        notifications.stream().filter(notification -> !notification.getSeen()).forEach(notification -> {
            notification.setSeen(true);
            repository.save(notification);
        });
    }
}