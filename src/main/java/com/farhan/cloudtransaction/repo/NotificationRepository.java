package com.farhan.cloudtransaction.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.*;

import org.springframework.stereotype.Repository;

import com.farhan.cloudtransaction.entity.Notification;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificationRepository {
    private final DynamoDbTable<Notification> notificationTable;
    private static final Logger logger = LoggerFactory.getLogger(NotificationRepository.class);

    public NotificationRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.notificationTable = enhancedClient.table("Notifications", TableSchema.fromBean(Notification.class));
    }

    @PostConstruct
    public void createTableIfNotExists() {
        try {
            notificationTable.createTable(CreateTableEnhancedRequest.builder().build());
            logger.info("Notifications table created successfully!");
        } catch (ResourceInUseException e) {
            logger.info("Notifications table already exists");
        } catch (DynamoDbException e) {
            logger.error("Error creating Notifications table: {}", e.getMessage());
            throw new RuntimeException("Error creating Notifications table", e);
        }
    }

    public void saveNotification(Notification notification) {
        try {
            notificationTable.putItem(notification);
            logger.info("Notification {} saved successfully!", notification.getNotificationId());
        } catch (DynamoDbException e) {
            logger.error("Failed to save notification {}: {}", notification.getNotificationId(), e.getMessage());
            throw new RuntimeException("Error saving notification", e);
        }
    }

    public Notification getNotification(String notificationId) {
        try {
            Notification notification = notificationTable.getItem(Key.builder().partitionValue(notificationId).build());
            if (notification == null) {
                logger.warn("Notification with ID {} not found", notificationId);
                throw new RuntimeException("Notification not found: " + notificationId);
            }
            return notification;
        } catch (DynamoDbException e) {
            logger.error("DynamoDB Error while fetching notification {}: {}", notificationId, e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }
    
    public Notification getNotificationByUploadToken(String uploadToken) {
        try {
            List<Notification> notifications = new ArrayList<>();
            notificationTable.scan(ScanEnhancedRequest.builder().build())
                .items()
                .forEach(notification -> {
                    if (uploadToken.equals(notification.getUploadToken())) {
                        notifications.add(notification);
                    }
                });
            
            if (notifications.isEmpty()) {
                logger.warn("No notification found with upload token: {}", uploadToken);
                throw new RuntimeException("No notification found with the provided upload token");
            }
            
            return notifications.get(0);
        } catch (DynamoDbException e) {
            logger.error("Error fetching notification by upload token: {}", e.getMessage());
            throw new RuntimeException("Error fetching notification", e);
        }
    }
    
    public List<Notification> getNotificationsByTransactionId(String transactionId) {
        try {
            List<Notification> notifications = new ArrayList<>();
            notificationTable.scan(ScanEnhancedRequest.builder().build())
                .items()
                .forEach(notification -> {
                    if (transactionId.equals(notification.getTransactionId())) {
                        notifications.add(notification);
                    }
                });
            return notifications;
        } catch (DynamoDbException e) {
            logger.error("Error fetching notifications for transaction {}: {}", transactionId, e.getMessage());
            throw new RuntimeException("Error fetching notifications", e);
        }
    }
} 