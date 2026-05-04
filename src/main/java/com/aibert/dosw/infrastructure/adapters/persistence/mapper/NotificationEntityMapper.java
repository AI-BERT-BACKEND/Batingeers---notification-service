package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationEntityMapper {

    NotificationEntity toEntity(Notification notification);

    Notification toDomain(NotificationEntity entity);

    List<Notification> toDomainList(List<NotificationEntity> entities);
}
