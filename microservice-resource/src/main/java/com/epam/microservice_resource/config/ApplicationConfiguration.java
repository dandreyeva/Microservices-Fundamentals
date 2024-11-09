package com.epam.microservice_resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class ApplicationConfiguration {
    @Value("${aws.s3.endpoint}")
    private String awsS3EndPoint;
    @Value("${aws.credentials.access-key}")
    protected String awsAccessKey;

    @Value("${aws.credentials.secret-key}")
    protected String awsSecretKey;

    @Value("${aws.region}")
    protected String awsRegion;

    @Bean
    public S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(amazonAWSCredentialsProvider())
                .endpointOverride(URI.create(awsS3EndPoint))
                .forcePathStyle(true)
                .build();
    }

    private AwsCredentialsProvider amazonAWSCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey));
    }

}
