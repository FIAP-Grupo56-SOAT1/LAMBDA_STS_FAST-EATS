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

######### OBS: a execution role acima foi trocada por LabRole devido a restricoes de permissao na conta da AWS Academy ########
variable "lab_role_arn" {
  type    = string
  default = "arn:aws:iam::730335661438:role/LabRole" #aws_iam_role.ecsTaskExecutionRole.arn
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



