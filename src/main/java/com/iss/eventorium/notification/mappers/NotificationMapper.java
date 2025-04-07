package com.iss.eventorium.notification.mappers;

import com.iss.eventorium.notification.dtos.NotificationResponseDto;
import com.iss.eventorium.notification.models.Notification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ModelMapper modelMapper;

    public NotificationResponseDto toResponse(Notification notification) {
        return modelMapper.map(notification, NotificationResponseDto.class);
    }
}
