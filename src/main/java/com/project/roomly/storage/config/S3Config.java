package com.project.roomly.storage.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;


import java.net.URI;

@Slf4j
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${storage.endpointUrl}") String endpointUrl,
            @Value("${storage.access_key}") String accessKey,
            @Value("${storage.secret_key}") String secretKey
    ) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of("ru-msk-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .build())
                .build();
    }

    @Bean
    public Tika tika(){
        return new Tika();
    }
}
