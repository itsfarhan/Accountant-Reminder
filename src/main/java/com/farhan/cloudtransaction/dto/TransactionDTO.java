package com.farhan.cloudtransaction.dto;

import com.farhan.cloudtransaction.entity.Transaction;
import java.time.LocalDateTime;

public record TransactionDTO(
    String transactionId,
    String clientId,
    String clientEmail,
    String description,
    boolean isCompleted,
    String fileKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public TransactionDTO(Transaction transaction) {
        this(
            transaction.getTransactionId(),
            transaction.getClientId(),
            transaction.getClientEmail(),
            transaction.getDescription(),
            transaction.getIsCompleted(),
            transaction.getFileKey(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt()
        );
    }
}
