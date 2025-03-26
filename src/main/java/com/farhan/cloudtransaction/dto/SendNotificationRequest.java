package com.farhan.cloudtransaction.dto;

import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequest(
    @NotBlank(message = "Transaction ID is required")
    String transactionId,
    
    @NotBlank(message = "Subject is required")
    String subject,
    
    @NotBlank(message = "Message is required")
    String message
) {
    // Compact canonical constructor for validation
    public SendNotificationRequest {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject cannot be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
    }
} 