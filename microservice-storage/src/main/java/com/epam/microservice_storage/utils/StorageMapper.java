package com.epam.microservice_storage.utils;

import com.epam.microservice_storage.dto.StorageRequestDTO;
import com.epam.microservice_storage.model.Storage;
import org.springframework.stereotype.Component;

@Component
public class StorageMapper {

    public Storage toStorage(StorageRequestDTO storageRequestDTO) {
        Storage storage  = new Storage();
        storage.setStorageType(storageRequestDTO.getStorageType());
        storage.setBucket(storageRequestDTO.getBucket());

        storage.setPath(storageRequestDTO.getPath());
        return storage;
    }
}
