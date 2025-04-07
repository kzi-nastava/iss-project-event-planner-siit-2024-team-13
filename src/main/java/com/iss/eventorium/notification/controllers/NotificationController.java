package com.iss.eventorium.notification.controllers;

import com.iss.eventorium.notification.dtos.NotificationResponseDto;
import com.iss.eventorium.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications() {
        return ResponseEntity.ok(service.getAllNotifications());
    }

    @PatchMapping("/seen")
    public ResponseEntity<Void> markNotificationsAsSeen() {
        service.markAsSeen();
        return ResponseEntity.noContent().build();
    }
}