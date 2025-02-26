package com.capstone1.sasscapstone1.service.NotificationService;

import com.capstone1.sasscapstone1.dto.NotificationDto.NotificationDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Notification;
import com.capstone1.sasscapstone1.entity.Account;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    ApiResponse<List<NotificationDto>> getNotificationsForUser(Account account, int pageNum, int pageSize);
    ApiResponse<Map<String, Long>> countNotificationOfUser(Account account);
    ResponseEntity<?> getUnreadNotificationsForUser(Account account);
    ApiResponse<List<NotificationDto>> getNotificationsSaved(Account account, int pageNum, int pageSize);
    ApiResponse<List<NotificationDto>> getNotificationsDeleted(Account account, int pageNum, int pageSize);
    ApiResponse<String> moveNotificationToTrash( List<Long> notificationIds);
    ApiResponse<String> moveNotificationToSaved( List<Long> notificationIds);
    ResponseEntity<Notification> createNotification(Account user, String message, String type);
    ApiResponse<String> markNotificationAsRead(List<Long> notificationIds);
    ApiResponse<String> deleteNotification(List<Long> notificationIds);
}
