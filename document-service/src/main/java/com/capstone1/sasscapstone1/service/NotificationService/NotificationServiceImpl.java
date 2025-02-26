package com.capstone1.sasscapstone1.service.NotificationService;

import com.capstone1.sasscapstone1.dto.NotificationDto.NotificationDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Notification;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import com.capstone1.sasscapstone1.repository.Notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    public ApiResponse<List<NotificationDto>> getNotificationsForUser(Account account, int pageNum, int pageSize) {
        try {
            PageRequest pageable = PageRequest.of(pageNum, pageSize);
            Page<Notification> notifications = notificationRepository.findByAccountOrderByCreatedAtDesc(account.getAccountId(), pageable);
            List<NotificationDto> notificationDtos = notifications.map(this::mapToDto).stream().toList();
            return CreateApiResponse.createResponse(notificationDtos,false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Long>> countNotificationOfUser(Account account) {
        try {
            Object[] result = notificationRepository.countNotifications(account.getAccountId());
            Object[] innerArray = (Object[]) result[0];
            Map<String, Long> counts = new HashMap<>();
            counts.put("total", ((Number) innerArray[0]).longValue());
            counts.put("saved", ((Number) innerArray[1]).longValue());
            counts.put("deleted", ((Number) innerArray[2]).longValue());
            counts.put("unRead", ((Number) innerArray[3]).longValue());
            return CreateApiResponse.createResponse(counts,false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getUnreadNotificationsForUser(Account account) {
        try {
            List<Notification> unreadNotifications = notificationRepository.findByAccountAndIsReadFalseOrderByCreatedAtDesc(account);
            List<NotificationDto> notificationDtos = unreadNotifications.stream().map(this::mapToDto).toList();
            return ResponseEntity.ok(notificationDtos);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<NotificationDto>> getNotificationsSaved(Account account, int pageNum, int pageSize) {
        try {
            PageRequest pageable = PageRequest.of(pageNum, pageSize);
            Page<Notification> notifications = notificationRepository.findNotifySaveByAccount(account.getAccountId(), pageable);
            List<NotificationDto> notificationDtos = notifications.map(this::mapToDto).stream().toList();
            return CreateApiResponse.createResponse(notificationDtos,false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<NotificationDto>> getNotificationsDeleted(Account account, int pageNum, int pageSize) {
        try {
            PageRequest pageable = PageRequest.of(pageNum, pageSize);
            Page<Notification> notifications = notificationRepository.findNotifyDeleteFlagByAccount(account.getAccountId(), pageable);
            List<NotificationDto> notificationDtos = notifications.map(this::mapToDto).stream().toList();
            return CreateApiResponse.createResponse(notificationDtos,false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> moveNotificationToTrash(List<Long> notificationIds) {
        try {
            for (Long notificationId : notificationIds) {
                Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
                if (optionalNotification.isPresent()) {
                    Notification findNotification = optionalNotification.get();
                    findNotification.setDeletedFlag(true);
                    findNotification.setIsSaved(false);
                    notificationRepository.save(findNotification);
                } else {
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Notification not found with ID: " + notificationId);
                }
            }
            return CreateApiResponse.createResponse("Chuyển vào thùng rác thành công",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> moveNotificationToSaved(List<Long> notificationIds) {
        try {
            for (Long notificationId : notificationIds) {
                Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
                if (optionalNotification.isPresent()) {
                    Notification findNotification = optionalNotification.get();
                    findNotification.setDeletedFlag(false);
                    findNotification.setIsSaved(true);
                    notificationRepository.save(findNotification);
                } else {
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Notification not found with ID: " + notificationId);
                }
            }
            return CreateApiResponse.createResponse("Lưu trữ thông báo thành công",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Notification> createNotification(Account account, String message, String type) {
        try {
            Notification notification = new Notification();
            notification.setAccount(account);
            notification.setMessage(message);
            notification.setType(type);
            notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error creating notification: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> markNotificationAsRead(List<Long> notificationIds) {
        try {
            for (Long notificationId : notificationIds) {
                Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
                if (optionalNotification.isPresent()) {
                    Notification notification = optionalNotification.get();
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                } else {
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Notification not found with ID: " + notificationId);
                }
            }
            return CreateApiResponse.createResponse("All notifications have been read",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error marking notification as read: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteNotification(List<Long> notificationIds) {
        try {
            for (Long notificationId : notificationIds) {
                Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
                if (optionalNotification.isPresent()) {
                    Notification notification = optionalNotification.get();
                    notificationRepository.delete(notification);
                } else {
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Notification not found with ID: " + notificationId);
                }
            }
            return CreateApiResponse.createResponse("Notification deleted successfully.",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error deleting notification: " + e.getMessage());
        }
    }
}
