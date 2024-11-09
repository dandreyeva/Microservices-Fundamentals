package com.epam.microservice_resource.repo;

import com.epam.microservice_resource.model.Resource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends CrudRepository<Resource, Integer> {
}
