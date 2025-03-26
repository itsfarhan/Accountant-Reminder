package com.farhan.cloudtransaction.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateTransactionRequest(
    @NotBlank(message = "Client ID is required")
    String clientId,
    
    @NotBlank(message = "Client email is required")
    @Email(message = "Invalid email format")
    String clientEmail,
    
    @NotBlank(message = "Description is required")
    String description
) {
    // Compact canonical constructor for validation
    public CreateTransactionRequest {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client ID cannot be blank");
        }
        if (clientEmail == null || clientEmail.isBlank()) {
            throw new IllegalArgumentException("Client email cannot be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
    }
} 