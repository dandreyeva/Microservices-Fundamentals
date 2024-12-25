package com.epam.microservice_storage.service;

import com.epam.microservice_storage.model.Storage;
import com.epam.microservice_storage.repo.StorageRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageService {

    private StorageRepository storageRepository;

    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    public int saveStorage(Storage storage) {
        return storageRepository.save(storage).getId();
    }

    public Iterable<Storage> getStorageList() {
        return storageRepository.findAll();
    }

    public void removeStorageById(int id) {
        storageRepository.deleteById(id);
    }

    public boolean existById(int id) {
        return storageRepository.existsById(id);
    }

    public boolean existStorage(String type) {
        return storageRepository.findStorageByStorageType(type) != null;
    }

    public Storage findStorageByType(String type) {
        return storageRepository.findStorageByStorageType(type);
    }

    public void removeStorageByType(String type) {
        storageRepository.removeStorageByStorageType(type);
    }
}
