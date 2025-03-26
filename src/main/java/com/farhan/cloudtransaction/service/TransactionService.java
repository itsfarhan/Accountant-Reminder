package com.farhan.cloudtransaction.service;

import com.farhan.cloudtransaction.dto.CreateTransactionRequest;
import com.farhan.cloudtransaction.dto.SendNotificationRequest;
import com.farhan.cloudtransaction.dto.TransactionDTO;
import com.farhan.cloudtransaction.entity.Notification;
import com.farhan.cloudtransaction.entity.Transaction;
import com.farhan.cloudtransaction.repo.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Value("${accountant.email}")
    private String accountantEmail;
    
    @Value("${app.fileDownloadExpiration}")
    private long fileDownloadExpirationHours;

    public TransactionService(TransactionRepository transactionRepository, S3Service s3Service,
            NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.s3Service = s3Service;
        this.notificationService = notificationService;
    }

    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setClientId(request.clientId());
        transaction.setClientEmail(request.clientEmail());
        transaction.setDescription(request.description());
        transaction.setIsCompleted(false);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.saveTransaction(transaction);
        logger.info("Created new transaction with ID: {}", transaction.getTransactionId());
        
        return new TransactionDTO(transaction);
    }

    public TransactionDTO getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.getTransaction(transactionId);
        return new TransactionDTO(transaction);
    }
    
    public List<TransactionDTO> getTransactionsByClientId(String clientId) {
        List<Transaction> transactions = transactionRepository.getTransactionsByClientId(clientId);
        return transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }

    public Notification sendNotification(SendNotificationRequest request) {
        Transaction transaction = transactionRepository.getTransaction(request.transactionId());
        
        Notification notification = notificationService.sendEmailNotification(
                transaction.getClientEmail(),
                transaction.getTransactionId(),
                request.subject(),
                request.message()
        );
        
        // Update transaction with notification ID
        transaction.setNotificationId(notification.getNotificationId());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.saveTransaction(transaction);
        
        logger.info("Sent notification for transaction: {}", transaction.getTransactionId());
        return notification;
    }
    
    public void validateUploadToken(String uploadToken) {
        // This will throw an exception if the token is invalid
        notificationService.getNotificationByUploadToken(uploadToken);
        logger.info("Upload token validated: {}", uploadToken);
    }

    public void attachFileToTransaction(String uploadToken, MultipartFile file) {
        // Get notification by upload token
        Notification notification = notificationService.getNotificationByUploadToken(uploadToken);
        String transactionId = notification.getTransactionId();
        
        // Get transaction
        Transaction transaction = transactionRepository.getTransaction(transactionId);
        
        // Upload file to S3
        String fileKey = s3Service.uploadFile(file);
        transaction.setFileKey(fileKey);
        transaction.setIsCompleted(true);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // Save updated transaction
        transactionRepository.saveTransaction(transaction);
        
        // Mark notification as resolved
        notificationService.markNotificationAsResolved(notification.getNotificationId());
        
        // Generate download URL for accountant
        String downloadUrl = s3Service.generatePresignedDownloadUrl(
                fileKey, 
                Duration.ofHours(fileDownloadExpirationHours)
        );
        
        // Notify accountant
        notificationService.sendAccountantNotification(
                accountantEmail,
                transactionId,
                fileKey,
                downloadUrl
        );
        
        logger.info("File attached to transaction {} and accountant notified", transactionId);
    }

    public byte[] downloadTransactionFile(String transactionId) {
        Transaction transaction = transactionRepository.getTransaction(transactionId);
        if (transaction.getFileKey() == null) {
            logger.error("No file found for transaction: {}", transactionId);
            throw new RuntimeException("No file found for this transaction");
        }

        return s3Service.downloadFile(transaction.getFileKey());
    }
    
    public String getFileDownloadUrl(String transactionId) {
        Transaction transaction = transactionRepository.getTransaction(transactionId);
        if (transaction.getFileKey() == null) {
            logger.error("No file found for transaction: {}", transactionId);
            throw new RuntimeException("No file found for this transaction");
        }
        
        return s3Service.generatePresignedDownloadUrl(
                transaction.getFileKey(),
                Duration.ofHours(fileDownloadExpirationHours)
        );
    }
}
