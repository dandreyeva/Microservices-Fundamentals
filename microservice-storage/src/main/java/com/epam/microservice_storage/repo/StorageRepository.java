package com.epam.microservice_storage.repo;

import com.epam.microservice_storage.model.Storage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends CrudRepository<Storage, Integer> {
}
