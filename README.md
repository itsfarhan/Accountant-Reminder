# Cloud Transaction Application

A simple, cost-effective solution for accountants to manage transaction documents from clients. This application allows accountants to request missing transaction documents from clients, provides clients with a secure upload link, and notifies the accountant when documents are uploaded.

## Features

- **Transaction Management**: Create and track transactions for clients
- **Document Requests**: Send email notifications to clients requesting missing documents
- **Secure File Upload**: Generate unique, secure links for clients to upload documents
- **Automatic Notifications**: Notify the accountant when documents are uploaded
- **Document Access**: Download or access uploaded documents via secure links

## Technology Stack

- **Backend**: Java Spring Boot 3 with Java 17
- **Database**: Amazon DynamoDB (NoSQL)
- **Storage**: Amazon S3 for document storage
- **Notifications**: Amazon SES (Simple Email Service) for email notifications
- **Frontend**: Thymeleaf templates for the upload interface
- **Infrastructure**: AWS (EC2, DynamoDB, S3, SES)

## Project Structure

```
cloudtransaction/
├── src/main/
│   ├── java/com/farhan/cloudtransaction/
│   │   ├── config/           # AWS and application configuration
│   │   ├── controller/       # REST controllers and web endpoints
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # DynamoDB entity classes
│   │   ├── repo/             # Repository layer for data access
│   │   ├── service/          # Business logic services
│   │   └── AccountingAppApplication.java  # Main application class
│   └── resources/
│       ├── templates/        # Thymeleaf HTML templates
│       └── application.properties  # Application configuration
└── terraform/                # Infrastructure as Code
    ├── main.tf               # Main Terraform configuration
    ├── variables.tf          # Variable definitions
    ├── terraform.tfvars.example  # Example variable values
    └── README.md             # Terraform setup instructions
```

## Prerequisites

1. Java 17 or higher
2. Maven
3. AWS Account
4. Terraform (for infrastructure deployment)

## Local Development Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/cloudtransaction.git
cd cloudtransaction
```

### 2. Configure AWS Credentials

Set up your AWS credentials using one of these methods:

- **AWS CLI**: Run `aws configure`
- **Environment Variables**:
  ```bash
  export AWS_ACCESS_KEY_ID="your-access-key"
  export AWS_SECRET_ACCESS_KEY="your-secret-key"
  export AWS_REGION="us-east-1"
  ```

### 3. Configure Application Properties

Create a `src/main/resources/application-dev.properties` file:

```properties
# AWS Configuration
aws.region=us-east-1
aws.s3.bucketName=your-bucket-name
aws.ses.senderEmail=your-verified-email@example.com

# Application Configuration
app.baseUrl=http://localhost:8080
app.fileDownloadExpiration=24
accountant.email=accountant@example.com

# Spring Configuration
spring.profiles.active=dev
```

> **Note about SNS references**: You might notice references to SNS (Simple Notification Service) in the application.properties file (`aws.sns.topic-arn=${SNS_TOPIC_ARN}`). This is a legacy reference from the original application which used SNS for additional notification channels. In our simplified implementation, we've removed SNS dependency and use only SES for email notifications to reduce costs and complexity. You can safely ignore these SNS references.

### 4. Build and Run Locally

```bash
mvn clean package
java -jar target/cloudtransaction-0.0.1-SNAPSHOT.jar
```

The application will be available at http://localhost:8080

## AWS Deployment

### 1. Set Up Infrastructure with Terraform

Navigate to the terraform directory:

```bash
cd terraform
```

Follow the instructions in the [Terraform README](terraform/README.md) to:
1. Configure AWS credentials
2. Customize your infrastructure settings
3. Deploy the infrastructure

### 2. Deploy the Application

After the infrastructure is set up:

1. SSH into your EC2 instance:
   ```bash
   ssh -i your-key.pem ec2-user@your-instance-ip
   ```

2. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/cloudtransaction.git
   cd cloudtransaction
   ```

3. Build and run the application:
   ```bash
   mvn clean package
   java -jar target/cloudtransaction-0.0.1-SNAPSHOT.jar
   ```

## Usage Guide

### For Accountants

1. **Create a Transaction**:
   - Use the API endpoint `POST /api/transactions` with client details
   - Example:
     ```json
     {
       "clientId": "client123",
       "clientEmail": "client@example.com",
       "description": "January 2023 Expense Report"
     }
     ```

2. **Request Missing Documents**:
   - Use the API endpoint `POST /api/transactions/{id}/notify`
   - Example:
     ```json
     {
       "subject": "Missing Receipt",
       "message": "Please upload the receipt for your January 2023 expense report."
     }
     ```

3. **Access Uploaded Documents**:
   - Use the API endpoint `GET /api/transactions/{id}/download-url`
   - Or download directly with `GET /api/transactions/{id}/download`

### For Clients

1. **Receive Document Request**:
   - Clients receive an email with a secure upload link

2. **Upload Documents**:
   - Click the link in the email
   - Use the upload form to select and upload the requested document
   - Receive confirmation when the upload is complete

## Notification System

Our application uses **Amazon SES (Simple Email Service)** for all notifications:

1. **Client Notifications**: When an accountant requests a document, the client receives an email with a secure upload link.
2. **Accountant Notifications**: When a client uploads a document, the accountant receives an email with a link to access the document.

This email-only approach was chosen for simplicity and cost-effectiveness, as it meets the core requirements without additional complexity.

## API Documentation

### Transactions

- `POST /api/transactions` - Create a new transaction
- `GET /api/transactions/{id}` - Get transaction details
- `GET /api/transactions/client/{clientId}` - Get all transactions for a client
- `POST /api/transactions/{id}/notify` - Send notification requesting documents
- `GET /api/transactions/{id}/download` - Download transaction file
- `GET /api/transactions/{id}/download-url` - Get a pre-signed URL to download the file

### File Upload

- `GET /upload/{token}` - Display upload form for clients
- `POST /upload/{token}` - Handle file upload from clients
- `GET /upload/success` - Display success page after upload
- `POST /upload/api/{token}` - API endpoint for programmatic uploads

## Security Considerations

- All S3 buckets are configured with server-side encryption
- Public access to S3 buckets is blocked
- EC2 instances use IAM roles with least privilege
- SSH access is restricted to your IP address
- Secure upload tokens are unique and single-use

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot for the application framework
- AWS for cloud infrastructure
- Terraform for infrastructure as code 