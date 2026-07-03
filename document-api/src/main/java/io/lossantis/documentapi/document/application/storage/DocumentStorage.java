package io.lossantis.documentapi.document.application.storage;

import java.io.InputStream;

public interface DocumentStorage {

    String upload(
            InputStream inputStream,
            long size,
            String contentType,
            String originalFilename
    );
}
