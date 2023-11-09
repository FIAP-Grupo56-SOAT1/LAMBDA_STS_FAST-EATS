locals {
  lambda_payload_filename = "../app/target/${var.lambda_function_name}-${var.version_lambda}.jar"
}



resource "aws_lambda_function" "lambda_sts" {
  function_name = var.lambda_function_name
  handler       = var.lambda_handler
  description   = var.description
  role          = aws_iam_role.lambda_sts_lambda.arn
  runtime       = var.lambda_runtime

  filename         = local.lambda_payload_filename
  source_code_hash = base64sha256(filebase64(local.lambda_payload_filename))

  timeout     = var.timeout
  memory_size = var.lambda_memory

  environment {
    variables = {
      USER_POOL_ID : var.user_pool_id
      CLIENTE_ID : jsondecode(data.aws_secretsmanager_secret_version.sts_credentials.secret_string)["client_id"]
      ACCESS_KEY : jsondecode(data.aws_secretsmanager_secret_version.sts_credentials.secret_string)["access_key"]
      SECRET_KEY : jsondecode(data.aws_secretsmanager_secret_version.sts_credentials.secret_string)["secret_key"]
      USER_COGNITO : jsondecode(data.aws_secretsmanager_secret_version.sts_credentials.secret_string)["user_cognito"]
      PASSWORD_COGNITO : jsondecode(data.aws_secretsmanager_secret_version.sts_credentials.secret_string)["password_cognito"]

      containerDbName : jsondecode(data.aws_secretsmanager_secret_version.mysql_credentials.secret_string)["dbname"]
      containerDbUser : jsondecode(data.aws_secretsmanager_secret_version.mysql_credentials.secret_string)["username"]
      containerDbPassword : jsondecode(data.aws_secretsmanager_secret_version.mysql_credentials.secret_string)["password"]
      containerDbRootPassword : jsondecode(data.aws_secretsmanager_secret_version.mysql_credentials.secret_string)["password"]
      containerDbServer : jsondecode(data.aws_secretsmanager_secret_version.mysql_credentials.secret_string)["host"]
      containerDbPort : jsondecode(data.aws_secretsmanager_secret_version.mysql_credentials.secret_string)["port"]
      NLB_API : var.nlb
    }
  }

}

#obteando dados do secret manager MySQL
data "aws_secretsmanager_secret" "mysql" {
  name = "prod/soat1grupo56/MySQL"
}

data "aws_secretsmanager_secret_version" "mysql_credentials" {
  secret_id = data.aws_secretsmanager_secret.mysql.id
}

#obteando dados do secret manager STS
data "aws_secretsmanager_secret" "sts" {
  name = "prod/soat1grupo56/STS"
}

data "aws_secretsmanager_secret_version" "sts_credentials" {
  secret_id = data.aws_secretsmanager_secret.sts.id
}

