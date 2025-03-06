package org.com.notificationservice.repository;

import org.com.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    @Query("select n from Notification n where n.accountId=:accountId and n.isSaved=false and n.deletedFlag=false order by n.isRead asc, n.createdAt desc")
    Page<Notification> findByAccountOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);

    // Lấy tất cả thông báo chưa đọc
    List<Notification> findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(Long accountId);

    Long countByAccountId(Long accountId);
    Long countNotificationByAccountIdAndIsSavedAndDeletedFlag(Long accountId, boolean isSaved, boolean isDeleted);
    Long countNotificationByAccountIdAndDeletedFlag(Long accountId, Boolean isDeleted);

    @Query("SELECT " +
            "SUM(CASE WHEN n.deletedFlag = false AND n.isSaved=false THEN 1 ELSE 0 END) AS total, " +
            "SUM(CASE WHEN n.isSaved = true AND n.deletedFlag = false THEN 1 ELSE 0 END) AS saved, " +
            "SUM(CASE WHEN n.deletedFlag = true THEN 1 ELSE 0 END) AS deleted, " +
            "SUM(CASE WHEN n.isRead=false THEN 1 ELSE 0 END) AS unRead " +
            "FROM Notification n WHERE n.accountId = :accountId")
    Object[] countNotifications(@Param("accountId") Long accountId);

    @Query("select n from Notification n where n.accountId=:accountId and n.isSaved=true and n.deletedFlag=false order by n.createdAt desc")
    Page<Notification> findNotifySaveByAccount(Long accountId, Pageable pageable);

    @Query("select n from Notification n where n.accountId=:accountId and n.deletedFlag=true order by n.createdAt desc")
    Page<Notification> findNotifyDeleteFlagByAccount(Long accountId, Pageable pageable);
}
