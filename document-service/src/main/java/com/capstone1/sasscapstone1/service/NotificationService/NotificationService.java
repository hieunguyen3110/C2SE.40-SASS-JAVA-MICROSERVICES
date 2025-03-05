package com.capstone1.sasscapstone1.service.NotificationService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.NotificationDto.NotificationDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Notification;
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
    ResponseEntity<Notification> createNotification(AccountDto user, String message, String type);
    ApiResponse<String> markNotificationAsRead(List<Long> notificationIds);
    ApiResponse<String> deleteNotification(List<Long> notificationIds);
}
