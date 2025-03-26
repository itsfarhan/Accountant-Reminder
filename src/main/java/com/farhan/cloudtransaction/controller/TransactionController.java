package com.farhan.cloudtransaction.controller;

import com.farhan.cloudtransaction.dto.ApiResponse;
import com.farhan.cloudtransaction.dto.CreateTransactionRequest;
import com.farhan.cloudtransaction.dto.SendNotificationRequest;
import com.farhan.cloudtransaction.dto.TransactionDTO;
import com.farhan.cloudtransaction.entity.Notification;
import com.farhan.cloudtransaction.service.TransactionService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        try {
            TransactionDTO transaction = transactionService.createTransaction(request);
            return ResponseEntity.ok(ApiResponse.success("Transaction created successfully", transaction));
        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create transaction: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(@PathVariable String id) {
        try {
            TransactionDTO transaction = transactionService.getTransactionById(id);
            return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", transaction));
        } catch (Exception e) {
            logger.error("Error retrieving transaction {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve transaction: " + e.getMessage()));
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByClient(@PathVariable String clientId) {
        try {
            List<TransactionDTO> transactions = transactionService.getTransactionsByClientId(clientId);
            return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            logger.error("Error retrieving transactions for client {}: {}", clientId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve transactions: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<ApiResponse<Notification>> sendNotification(
            @PathVariable String id, 
            @Valid @RequestBody SendNotificationRequest request) {
        try {
            // Override the transaction ID from the path parameter
            SendNotificationRequest updatedRequest = new SendNotificationRequest(
                    id, 
                    request.subject(), 
                    request.message()
            );
            
            Notification notification = transactionService.sendNotification(updatedRequest);
            return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", notification));
        } catch (Exception e) {
            logger.error("Error sending notification for transaction {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send notification: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        try {
            byte[] fileData = transactionService.downloadTransactionFile(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id + "-document")
                    .body(fileData);
        } catch (Exception e) {
            logger.error("Error downloading file for transaction {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/{id}/download-url")
    public ResponseEntity<ApiResponse<String>> getDownloadUrl(@PathVariable String id) {
        try {
            String downloadUrl = transactionService.getFileDownloadUrl(id);
            return ResponseEntity.ok(ApiResponse.success("Download URL generated successfully", downloadUrl));
        } catch (Exception e) {
            logger.error("Error generating download URL for transaction {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to generate download URL: " + e.getMessage()));
        }
    }
}
