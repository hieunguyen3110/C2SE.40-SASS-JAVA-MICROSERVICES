package org.com.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.notificationservice.dto.response.AccountDto;
import org.com.notificationservice.dto.response.ApiResponse;
import org.com.notificationservice.dto.response.NotificationDto;
import org.com.notificationservice.entity.Notification;
import org.com.notificationservice.enums.ErrorCode;
import org.com.notificationservice.exception.ApiException;
import org.com.notificationservice.helpers.CreateApiResponse;
import org.com.notificationservice.mapper.NotificationMapper;
import org.com.notificationservice.repository.NotificationRepository;
import org.com.notificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
    public ApiResponse<List<NotificationDto>> getNotificationsForUser(AccountDto account, int pageNum, int pageSize) {
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
    public ApiResponse<Map<String, Long>> countNotificationOfUser(AccountDto account) {
        try {
            Object[] result = notificationRepository.countNotifications(account.getAccountId());
            Object[] innerArray = (Object[]) result[0];
            Map<String, Long> counts = new HashMap<>();
            counts.put("total", innerArray[0] != null ? ((Number) innerArray[0]).longValue() : 0);
            counts.put("saved", innerArray[1] != null ? ((Number) innerArray[1]).longValue() : 0);
            counts.put("deleted", innerArray[2] != null ? ((Number) innerArray[2]).longValue() : 0);
            counts.put("unRead", innerArray[3] != null ? ((Number) innerArray[3]).longValue() : 0);
            return CreateApiResponse.createResponse(counts,false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getUnreadNotificationsForUser(AccountDto account) {
        try {
            List<Notification> unreadNotifications = notificationRepository.findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(account.getAccountId());
            List<NotificationDto> notificationDtos = unreadNotifications.stream().map(this::mapToDto).toList();
            return ResponseEntity.ok(notificationDtos);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<NotificationDto>> getNotificationsSaved(AccountDto account, int pageNum, int pageSize) {
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
    public ApiResponse<List<NotificationDto>> getNotificationsDeleted(AccountDto account, int pageNum, int pageSize) {
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
    public ApiResponse<NotificationDto> createNotification(AccountDto account, String message, String type) {
        try {
            Notification notification = new Notification();
            notification.setAccountId(account.getAccountId());
            notification.setMessage(message);
            notification.setType(type);
            Notification notificationSave=notificationRepository.save(notification);
            return CreateApiResponse.createResponse(NotificationMapper.mapToNotificationDto(notificationSave),false);
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
