package com.cheolhyeon.diary.app.notification.entity;

import com.cheolhyeon.diary.app.notification.enums.EventType;
import com.cheolhyeon.diary.app.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tx_id")
    private String transactionId;

    private Long recipientId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int attemptCount;
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
