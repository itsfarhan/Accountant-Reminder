variable "aws_region" {
  description = "The AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "s3_bucket_name" {
  description = "The name of the S3 bucket for transaction files"
  type        = string
  default     = "accounting-transaction-files-unique-name" # Change this to a globally unique name
}

variable "sender_email" {
  description = "The email address to send notifications from"
  type        = string
  default     = "notifications@yourdomain.com" # Change this to your verified email
}

variable "accountant_email" {
  description = "The accountant's email address"
  type        = string
  default     = "accountant@yourdomain.com" # Change this to the accountant's email
}

variable "your_ip" {
  description = "Your IP address for SSH access (x.x.x.x)"
  type        = string
  default     = "0.0.0.0" # Change this to your IP address
}

variable "ami_id" {
  description = "The AMI ID for the EC2 instance"
  type        = string
  default     = "ami-0c55b159cbfafe1f0" # Amazon Linux 2 AMI (update for your region)
}

variable "key_name" {
  description = "The name of the key pair for SSH access"
  type        = string
  default     = "your-key-pair" # Change this to your key pair name
} 