package com.farhan.cloudtransaction.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.*;

import org.springframework.stereotype.Repository;

import com.farhan.cloudtransaction.entity.Transaction;

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
public class TransactionRepository {
    private final DynamoDbTable<Transaction> transactionTable;
    private static final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);

    public TransactionRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.transactionTable = enhancedClient.table("Transactions", TableSchema.fromBean(Transaction.class));
    }

    @PostConstruct
    public void createTableIfNotExists() {
        try {
            transactionTable.createTable(CreateTableEnhancedRequest.builder().build());
            logger.info("Transactions table created successfully!");
        } catch (ResourceInUseException e) {
            logger.info("Transactions table already exists");
        } catch (DynamoDbException e) {
            logger.error("Error creating Transactions table: {}", e.getMessage());
            throw new RuntimeException("Error creating Transactions table", e);
        }
    }

    public void saveTransaction(Transaction transaction) {
        try {
            transactionTable.putItem(transaction);
            logger.info("Transaction {} saved successfully!", transaction.getTransactionId());
        } catch (DynamoDbException e) {
            logger.error("Failed to save transaction {}: {}", transaction.getTransactionId(), e.getMessage());
            throw new RuntimeException("Error saving transaction", e);
        }
    }

    public Transaction getTransaction(String transactionId) {
        try {
            Transaction transaction = transactionTable.getItem(Key.builder().partitionValue(transactionId).build());
            if (transaction == null) {
                logger.warn("Transaction with ID {} not found", transactionId);
                throw new RuntimeException("Transaction not found: " + transactionId);
            }
            return transaction;
        } catch (DynamoDbException e) {
            logger.error("DynamoDB Error while fetching transaction {}: {}", transactionId, e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }
    
    public List<Transaction> getTransactionsByClientId(String clientId) {
        try {
            List<Transaction> transactions = new ArrayList<>();
            transactionTable.scan(ScanEnhancedRequest.builder().build())
                .items()
                .forEach(transaction -> {
                    if (clientId.equals(transaction.getClientId())) {
                        transactions.add(transaction);
                    }
                });
            return transactions;
        } catch (DynamoDbException e) {
            logger.error("Error fetching transactions for client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Error fetching transactions", e);
        }
    }
}
