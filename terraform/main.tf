provider "aws" {
  region = var.aws_region
}

# S3 Bucket for storing transaction files
resource "aws_s3_bucket" "transaction_files" {
  bucket = var.s3_bucket_name
}

# S3 Bucket encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "transaction_files_encryption" {
  bucket = aws_s3_bucket.transaction_files.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# S3 Bucket public access block
resource "aws_s3_bucket_public_access_block" "transaction_files_access" {
  bucket = aws_s3_bucket.transaction_files.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# DynamoDB Table for transactions
resource "aws_dynamodb_table" "transactions" {
  name         = "Transactions"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "transactionId"

  attribute {
    name = "transactionId"
    type = "S"
  }
}

# DynamoDB Table for notifications
resource "aws_dynamodb_table" "notifications" {
  name         = "Notifications"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "notificationId"

  attribute {
    name = "notificationId"
    type = "S"
  }
}

# SES Email Identity for sender
resource "aws_ses_email_identity" "sender" {
  email = var.sender_email
}

# SES Email Identity for accountant
resource "aws_ses_email_identity" "accountant" {
  email = var.accountant_email
}

# EC2 Security Group
resource "aws_security_group" "app_sg" {
  name        = "accounting-app-sg"
  description = "Security group for accounting application"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["${var.your_ip}/32"]
    description = "SSH access"
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Application access"
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP access"
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS access"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }
}

# IAM Role for EC2
resource "aws_iam_role" "app_role" {
  name = "accounting-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })
}

# IAM Policy for the application
resource "aws_iam_policy" "app_policy" {
  name        = "accounting-app-policy"
  description = "Policy for the accounting application"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ],
        Resource = [
          aws_s3_bucket.transaction_files.arn,
          "${aws_s3_bucket.transaction_files.arn}/*"
        ]
      },
      {
        Effect = "Allow",
        Action = [
          "dynamodb:PutItem",
          "dynamodb:GetItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Scan",
          "dynamodb:Query",
          "dynamodb:CreateTable"
        ],
        Resource = [
          aws_dynamodb_table.transactions.arn,
          aws_dynamodb_table.notifications.arn
        ]
      },
      {
        Effect = "Allow",
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail"
        ],
        Resource = "*"
      }
    ]
  })
}

# Attach policy to role
resource "aws_iam_role_policy_attachment" "app_policy_attachment" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.app_policy.arn
}

# EC2 Instance Profile
resource "aws_iam_instance_profile" "app_profile" {
  name = "accounting-app-profile"
  role = aws_iam_role.app_role.name
}

# EC2 Instance
resource "aws_instance" "app_instance" {
  ami                    = var.ami_id
  instance_type          = "t2.micro"
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.app_sg.id]
  iam_instance_profile   = aws_iam_instance_profile.app_profile.name

  user_data = <<-EOF
              #!/bin/bash
              sudo yum update -y
              sudo amazon-linux-extras install java-openjdk17 -y
              sudo yum install -y git maven
              
              # Create application.properties
              mkdir -p /home/ec2-user/app/src/main/resources
              cat > /home/ec2-user/app/src/main/resources/application.properties << 'EOL'
              # Server configuration
              server.port=8080
              spring.application.name=cloud-transaction

              # AWS Configuration
              aws.region=${var.aws_region}
              aws.s3.bucketName=${var.s3_bucket_name}
              aws.ses.senderEmail=${var.sender_email}

              # Application Configuration
              app.baseUrl=http://${self.public_ip}:8080
              app.fileDownloadExpiration=24
              accountant.email=${var.accountant_email}

              # Logging Configuration
              logging.level.root=INFO
              logging.level.com.farhan.cloudtransaction=DEBUG
              logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

              # Multipart File Upload Configuration
              spring.servlet.multipart.max-file-size=10MB
              spring.servlet.multipart.max-request-size=10MB

              # Thymeleaf Configuration
              spring.thymeleaf.cache=false
              spring.thymeleaf.enabled=true
              spring.thymeleaf.prefix=classpath:/templates/
              spring.thymeleaf.suffix=.html
              EOL
              
              # Set permissions
              chown -R ec2-user:ec2-user /home/ec2-user/app
              
              echo "Infrastructure setup complete. Clone your repository and deploy the application."
              EOF

  tags = {
    Name = "accounting-app-instance"
  }
}

# Output the public IP of the EC2 instance
output "app_instance_public_ip" {
  value = aws_instance.app_instance.public_ip
}

# Output the S3 bucket name
output "s3_bucket_name" {
  value = aws_s3_bucket.transaction_files.bucket
}

# Output instructions for next steps
output "next_steps" {
  value = <<-EOT
    Infrastructure has been created successfully!
    
    Next steps:
    1. SSH into your EC2 instance:
       ssh -i ${var.key_name}.pem ec2-user@${aws_instance.app_instance.public_ip}
    
    2. Clone your repository:
       git clone https://github.com/yourusername/cloudtransaction.git
       cd cloudtransaction
    
    3. Build and run the application:
       mvn clean package
       java -jar target/cloudtransaction-0.0.1-SNAPSHOT.jar
    
    4. Access your application at:
       http://${aws_instance.app_instance.public_ip}:8080
  EOT
} 