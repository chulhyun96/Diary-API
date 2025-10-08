package com.cheolhyeon.diary.app.notification.repository;

import com.cheolhyeon.diary.app.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    @Modifying
    @Query(value = """
            INSERT INTO notification_log
              (tx_id, recipient_id, event_type, status, attempt_count, created_at, updated_at)
            VALUES
              (:txId, :recipientId, :eventType, 'NEW', 1, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
              attempt_count = attempt_count + 1,
              updated_at = NOW()
            """, nativeQuery = true)
    int upsertNewAttempt(@Param("txId") String txId,
                         @Param("recipientId") Long recipientId,
                         @Param("eventType") String eventType);


    @Modifying
    @Query(value = """
            UPDATE notification_log
            SET status = :status,
                error_message = :errorMessage,
                updated_at = NOW()
            WHERE tx_id = :txId AND event_type = :eventType
            """, nativeQuery = true)
    int updateStatus(@Param("txId") String txId,
                     @Param("eventType") String eventType,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage);
}
