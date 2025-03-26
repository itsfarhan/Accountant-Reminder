package com.farhan.cloudtransaction.dto;

import com.farhan.cloudtransaction.entity.Notification;
import java.time.LocalDateTime;

public record NotificationDTO(
    String notificationId,
    String transactionId,
    String recipientEmail,
    String subject,
    String message,
    LocalDateTime sentAt,
    Boolean isResolved
) {
    public NotificationDTO(Notification notification) {
        this(
            notification.getNotificationId(),
            notification.getTransactionId(),
            notification.getRecipientEmail(),
            notification.getSubject(),
            notification.getMessage(),
            notification.getSentAt(),
            notification.getIsResolved()
        );
    }
} 