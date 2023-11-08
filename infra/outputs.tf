output "lambdas" {
  value = [{
    arn           = aws_lambda_function.lambda_sts.arn
    name          = aws_lambda_function.lambda_sts.function_name
    description   = aws_lambda_function.lambda_sts.description
    version       = aws_lambda_function.lambda_sts.version
    last_modified = aws_lambda_function.lambda_sts.last_modified
  }]
}
