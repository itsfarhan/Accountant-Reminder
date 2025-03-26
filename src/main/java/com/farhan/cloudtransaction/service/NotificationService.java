package com.farhan.cloudtransaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import com.farhan.cloudtransaction.entity.Notification;
import com.farhan.cloudtransaction.repo.NotificationRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private final SesClient sesClient;
    private final NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Value("${aws.ses.senderEmail}")
    private String senderEmail;
    
    @Value("${app.baseUrl}")
    private String baseUrl;

    public NotificationService(SesClient sesClient, NotificationRepository notificationRepository) {
        this.sesClient = sesClient;
        this.notificationRepository = notificationRepository;
    }

    public Notification sendEmailNotification(String recipientEmail, String transactionId, String subject, String messageBody) {
        try {
            // Create a unique upload token
            String uploadToken = UUID.randomUUID().toString();
            
            // Create upload URL
            String uploadUrl = baseUrl + "/upload/" + uploadToken;
            
            // Add the upload link to the message
            String fullMessage = messageBody + "\n\nPlease upload the required document using this link: " + uploadUrl;
            
            // Create notification record
            Notification notification = new Notification();
            notification.setNotificationId(UUID.randomUUID().toString());
            notification.setTransactionId(transactionId);
            notification.setRecipientEmail(recipientEmail);
            notification.setSubject(subject);
            notification.setMessage(fullMessage);
            notification.setSentAt(LocalDateTime.now());
            notification.setUploadToken(uploadToken);
            notification.setIsResolved(false);
            
            // Save notification first
            notificationRepository.saveNotification(notification);
            
            // Send email
            Destination destination = Destination.builder()
                    .toAddresses(recipientEmail)
                    .build();

            Message emailMessage = Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder().text(Content.builder().data(fullMessage).build()).build())
                    .build();

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(destination)
                    .message(emailMessage)
                    .build();

            sesClient.sendEmail(emailRequest);
            logger.info("Email notification sent to {} for transaction {}", recipientEmail, transactionId);
            
            return notification;
        } catch (Exception e) {
            logger.error("Failed to send email notification: {}", e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
    
    public void sendAccountantNotification(String accountantEmail, String transactionId, String fileKey, String downloadUrl) {
        try {
            String subject = "Document Uploaded for Transaction " + transactionId;
            String message = "A document has been uploaded for transaction " + transactionId + ".\n\n" +
                    "You can download the document using this link: " + downloadUrl;
            
            Destination destination = Destination.builder()
                    .toAddresses(accountantEmail)
                    .build();

            Message emailMessage = Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder().text(Content.builder().data(message).build()).build())
                    .build();

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(destination)
                    .message(emailMessage)
                    .build();

            sesClient.sendEmail(emailRequest);
            logger.info("Accountant notification sent to {} for transaction {}", accountantEmail, transactionId);
        } catch (Exception e) {
            logger.error("Failed to send accountant notification: {}", e.getMessage());
            throw new RuntimeException("Failed to send accountant notification", e);
        }
    }
    
    public Notification getNotificationByUploadToken(String uploadToken) {
        return notificationRepository.getNotificationByUploadToken(uploadToken);
    }
    
    public void markNotificationAsResolved(String notificationId) {
        Notification notification = notificationRepository.getNotification(notificationId);
        notification.setIsResolved(true);
        notificationRepository.saveNotification(notification);
        logger.info("Notification {} marked as resolved", notificationId);
    }
} 