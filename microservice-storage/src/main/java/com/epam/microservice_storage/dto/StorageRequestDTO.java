package com.epam.microservice_storage.dto;

public class StorageRequestDTO {

    private String storageType;
    private String bucket;
    private String path;

    public StorageRequestDTO(String storageType, String bucket, String path) {
        this.storageType = storageType;
        this.bucket = bucket;
        this.path = path;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getBucket() {
        return bucket;
    }

    public String getPath() {
        return path;
    }
}
