package io.lossantis.documentprocessor.document.application.storage;

public interface DocumentStorage {
    byte[] download(String storageKey);
}