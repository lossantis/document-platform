package io.lossantis.documentapi.document.infrastructure.storage;

import io.lossantis.documentapi.document.application.storage.DocumentStorageKeyGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(MagaluObjectStorageProperties.class)
public class MagaluObjectStorageConfig {

    @Bean
    public S3Client s3Client(MagaluObjectStorageProperties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey(),
                properties.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public DocumentStorageKeyGenerator documentStorageKeyGenerator() {
        return new DocumentStorageKeyGenerator();
    }
}
