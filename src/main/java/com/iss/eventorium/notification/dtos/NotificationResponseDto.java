package com.iss.eventorium.notification.dtos;

import com.iss.eventorium.notification.models.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private String message;
    private Boolean seen;
    private LocalDateTime timestamp;
    private NotificationType type;
}
