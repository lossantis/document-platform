package io.lossantis.documentapi.document.infrastructure.storage;

import io.lossantis.documentapi.document.application.storage.DocumentStorage;
import io.lossantis.documentapi.document.application.storage.DocumentStorageKeyGenerator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Component
public class MagaluDocumentStorage implements DocumentStorage {

    private final S3Client s3Client;
    private final MagaluObjectStorageProperties properties;
    private final DocumentStorageKeyGenerator keyGenerator;

    public MagaluDocumentStorage(
            S3Client s3Client,
            MagaluObjectStorageProperties properties,
            DocumentStorageKeyGenerator keyGenerator
    ) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.keyGenerator = keyGenerator;
    }

    @Override
    public String upload(
            InputStream inputStream,
            long size,
            String contentType,
            String originalFilename
    ) {
        String key = keyGenerator.generate(originalFilename);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(inputStream, size)
        );

        return key;
    }
}