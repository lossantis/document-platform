package io.lossantis.documentprocessor.document.infrastructure.storage;

import io.lossantis.documentprocessor.document.application.storage.DocumentStorage;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Component
public class MagaluDocumentStorage implements DocumentStorage {

    private final S3Client s3Client;
    private final MagaluObjectStorageProperties properties;

    public MagaluDocumentStorage(
            S3Client s3Client,
            MagaluObjectStorageProperties properties
    ) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public byte[] download(String storageKey) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(storageKey)
                .build();

        ResponseBytes<GetObjectResponse> response =
                s3Client.getObjectAsBytes(request);

        return response.asByteArray();
    }
}