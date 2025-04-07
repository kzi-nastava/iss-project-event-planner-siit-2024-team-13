package com.iss.eventorium.notification.repositories;

import com.iss.eventorium.notification.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByRecipientIsNullOrderByTimestampDesc();
    List<Notification> findByRecipient_IdOrderByTimestampDesc(Long id);
}
