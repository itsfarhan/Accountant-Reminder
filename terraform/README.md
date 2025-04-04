# Cloud Transaction App - AWS Infrastructure

This directory contains Terraform configuration to set up the AWS infrastructure required for the Cloud Transaction application.

## Prerequisites

1. [Terraform](https://www.terraform.io/downloads.html) installed (v1.0.0 or newer)
2. AWS account
3. AWS CLI installed and configured with appropriate credentials
4. SSH key pair created in AWS

## Setup Instructions

### 1. Configure AWS Credentials

There are several ways to configure AWS credentials for Terraform:

#### Option 1: AWS CLI Configuration (Recommended for Development)

```bash
aws configure
```

This will prompt you for:
- AWS Access Key ID
- AWS Secret Access Key
- Default region
- Default output format

#### Option 2: Environment Variables

```bash
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
export AWS_REGION="us-east-1"
```

#### Option 3: Shared Credentials File

Create or edit `~/.aws/credentials`:

```
[default]
aws_access_key_id = your-access-key
aws_secret_access_key = your-secret-key
```

### 2. Customize Configuration

1. Copy the example variables file:

```bash
cp terraform.tfvars.example terraform.tfvars
```

2. Edit `terraform.tfvars` with your specific values:
   - Update `s3_bucket_name` to a globally unique name
   - Set `sender_email` and `accountant_email` to valid email addresses
   - Set `your_ip` to your public IP address for SSH access
   - Update `ami_id` if you're using a region other than us-east-1
   - Set `key_name` to the name of your SSH key pair in AWS

### 3. Initialize Terraform

```bash
terraform init
```

### 4. Preview the Changes

```bash
terraform plan
```

Review the planned changes to ensure they match your expectations.

### 5. Apply the Configuration

```bash
terraform apply
```

Type `yes` when prompted to confirm the creation of resources.

### 6. Access Your Infrastructure

After the infrastructure is created, Terraform will output:
- The public IP address of your EC2 instance
- The S3 bucket name
- Instructions for next steps

### 7. Clean Up

When you no longer need the infrastructure, you can destroy it:

```bash
terraform destroy
```

Type `yes` when prompted to confirm the deletion of resources.

## Security Best Practices

1. **Never commit `terraform.tfvars` to version control**. Add it to your `.gitignore` file.
2. **Restrict SSH access** to your IP address only.
3. **Use IAM roles with least privilege** for your EC2 instance.
4. **Enable encryption** for your S3 bucket and DynamoDB tables.
5. **Regularly rotate your AWS credentials**.

## Troubleshooting

1. **SSH Connection Issues**: Ensure your security group allows SSH from your IP address.
2. **Email Verification**: SES requires email verification in sandbox mode. Check your email for verification links.
3. **S3 Bucket Name**: If creation fails, try a different bucket name as they must be globally unique.

## Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS CLI Documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html)
- [AWS IAM Best Practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html) 