variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "lambda_function_name" {
  description = "LAMBDA Function name"
  type        = string
  default     = "lambda_sts"
}

variable "lambda_memory" {
  description = "Lambda max memory size"
  type        = number
  default     = 512
}

variable "lambda_runtime" {
  description = "Lambda runtime"
  type        = string
  default     = "java17"
}

variable "lambda_handler" {
  description = "Lambda handler"
  type        = string
  default     = "br.com.fiap.festeat.sts.AutenticarHandler::handleRequest"
}

variable "timeout" {
  description = "code version"
  type        = number
  default     = 15
}

variable "description" {
  description = "Descrição do lambda"
  type        = string
  default     = "lambda para sts de token - fiap 56"
}

variable "version_lambda" {
  description = "code version"
  type        = string
  default     = "1.0.0"
}

variable "user_pool_id" {
  description = "user_pool_id"
  type        = string
  default     = "us-east-1_5AektK0sI"
}

variable "client_id" {
  description = "client_id"
  type        = string
  default     = ""
}

variable "access_key" {
  description = "access_key"
  type        = string
  default     = ""
}
variable "secret_key" {
  description = "secret_key"
  type        = string
  default     = ""
}
variable "user_cognito" {
  description = "user_cognito"
  type        = string
  default     = "fiap56soat1"
}
variable "password_cognito" {
  description = "password_cognito"
  type        = string
  default     = ""
}

variable "nlb" {
  description = "nlb"
  type        = string
  default = "http://ecs-fasteats-299429471.us-east-1.elb.amazonaws.com:8080"
}


