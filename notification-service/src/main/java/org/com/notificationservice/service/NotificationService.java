package org.com.notificationservice.service;

import org.com.notificationservice.dto.response.AccountDto;
import org.com.notificationservice.dto.response.ApiResponse;
import org.com.notificationservice.dto.response.NotificationDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    ApiResponse<List<NotificationDto>> getNotificationsForUser(AccountDto account, int pageNum, int pageSize);
    ApiResponse<Map<String, Long>> countNotificationOfUser(AccountDto account);
    ResponseEntity<?> getUnreadNotificationsForUser(AccountDto account);
    ApiResponse<List<NotificationDto>> getNotificationsSaved(AccountDto account, int pageNum, int pageSize);
    ApiResponse<List<NotificationDto>> getNotificationsDeleted(AccountDto account, int pageNum, int pageSize);
    ApiResponse<String> moveNotificationToTrash( List<Long> notificationIds);
    ApiResponse<String> moveNotificationToSaved( List<Long> notificationIds);
    ApiResponse<NotificationDto> createNotification(AccountDto user, String message, String type);
    ApiResponse<String> markNotificationAsRead(List<Long> notificationIds);
    ApiResponse<String> deleteNotification(List<Long> notificationIds);
}
