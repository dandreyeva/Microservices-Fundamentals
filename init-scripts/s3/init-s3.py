import boto3

s3_client = boto3.client(
    "s3",
    endpoint_url=f"http://localstack:4566",
    aws_access_key_id="test_access_key",
    aws_secret_access_key="test_secret_access_key"
)

s3_client.create_bucket(Bucket="song-bucket")