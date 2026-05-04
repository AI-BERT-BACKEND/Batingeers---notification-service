package com.aibert.dosw.entrypoints.rest.mapper;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationRestMapper {

    public List<NotificationResponse> toSortedByUnread(List<NotificationResponse> notifications) {
        return notifications.stream()
                .sorted((a, b) -> Boolean.compare(a.isRead(), b.isRead()))
                .toList();
    }
}
