package org.com.notificationservice.mapper;

import org.com.notificationservice.dto.response.NotificationDto;
import org.com.notificationservice.entity.Notification;

public class NotificationMapper {
    public static Notification mapToNotification(NotificationDto notificationDto){
        return Notification.builder()
                .notificationId(notificationDto.getNotificationId())
                .accountId(notificationDto.getNotificationId())
                .type(notificationDto.getType())
                .message(notificationDto.getMessage())
                .deletedFlag(notificationDto.getDeletedFlag())
                .isRead(notificationDto.getIsRead())
                .isSaved(notificationDto.getIsSaved())
                .build();
    }
    public static NotificationDto mapToNotificationDto(Notification notification){
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .accountId(notification.getNotificationId())
                .type(notification.getType())
                .message(notification.getMessage())
                .deletedFlag(notification.getDeletedFlag())
                .isRead(notification.getIsRead())
                .isSaved(notification.getIsSaved())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
