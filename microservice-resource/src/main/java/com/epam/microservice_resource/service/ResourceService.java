package com.epam.microservice_resource.service;

import com.epam.microservice_resource.model.Resource;
import com.epam.microservice_resource.repo.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceService {

    private ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource saveResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public Optional<Resource> getResourceById(int id) {
        return resourceRepository.findById(id);
    }

    public void removeResourceById(int id) {
        resourceRepository.deleteById(id);
    }
    public boolean existById(int id) {
        return resourceRepository.existsById(id);
    }
}
