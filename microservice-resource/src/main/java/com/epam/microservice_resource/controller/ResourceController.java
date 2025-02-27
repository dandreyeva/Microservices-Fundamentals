package com.epam.microservice_resource.controller;

import com.epam.microservice_resource.model.Resource;
import com.epam.microservice_resource.service.MessageService;
import com.epam.microservice_resource.service.ResourceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.IOException;
import java.util.*;

@RestController
@Validated
@RequestMapping("/resources")
public class ResourceController {

    private ResourceService resourceService;
    private S3Client s3Client;
    @Value("${bucket.name.stage}")
    private String bucketNameStage;
    @Value("${bucket.name.permanent}")
    private String bucketNamePermanent;
    @Value("${spring.application.microservice-storage.name}")
    private String storageServiceName;
    private MessageService messageService;
    private DiscoveryClient discoveryClient;
    private RestTemplate restTemplate;
    private byte[] audioData;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);


    public ResourceController(ResourceService resourceService,
                              S3Client s3Client, MessageService messageService, DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.resourceService = resourceService;
        this.s3Client = s3Client;
        this.messageService = messageService;
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    @PostMapping(consumes = "audio/mpeg", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createResources(@RequestBody byte[] audioData,
                                                                @RequestHeader("fileName") String keyName) {
        var tika = new Tika();
        var type = tika.detect(audioData);//check valid and type of data

        if (keyName != null && type.equals("audio/mpeg")) {
            try {
                this.audioData = audioData;
                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketNameStage)
                                .key(keyName)
                                .build(),
                        software.amazon.awssdk.core.sync.RequestBody.fromBytes(audioData));
            } catch (S3Exception e) {
                System.exit(1);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Please, check S3 bucket settings");
            }

            Map<String, String> responseBody;
            var resource = new Resource();
            resource.setName(keyName);
            resource.setStage("STAGING");
            var  id = resourceService.saveResource(resource).getId().toString();

            Boolean result = messageService.sendQueueMessage("resourceIdQueue", id);
            responseBody = Map.of("id", id,
                                  "toQueue", result.toString());

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getResourceById(@PathVariable(value = "id") int resourceId) {
        String bucketName = "song-bucket-staging";
        if (resourceId > 0) {
            var resource = resourceService.getResourceById(resourceId);
            if (resource.isPresent()) {
                var resourceName = resource.get().getName();
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(resourceName)
                        .build();
                ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
                try {
                    return new ResponseEntity<>(response.readAllBytes(), HttpStatus.OK);
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource was not found");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }

    }

    public void removeResourcesById(String id, String bucketName) {
        resourceService.removeResourceById(Integer.parseInt(id));
        List<String> deletedListS3 = new ArrayList<>(Integer.parseInt(id));
        deleteMultipleObjects(bucketName, deletedListS3);
    }

    @RabbitListener(queues = ("processedResourceIdQueue"))
    @CircuitBreaker(name = "store", fallbackMethod = "fallbackForStoringResource")
    public void getSongSavingResult(String resourceId) {
        Resource resource = resourceService.getResourceById(Integer.parseInt(resourceId)).orElse(null);
        resource.setStage("PERMANENT");
        LOGGER.info("audioData" + audioData);
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketNamePermanent)
                            .key(resource.getName())
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(audioData));
            removeResourcesById(resourceId, bucketNameStage);
            resourceService.saveResource(resource);
            sendDeleteResourceFromStorageRequest(resource.getName(), bucketNameStage);
        } catch (S3Exception e) {
            System.exit(1);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Please, check S3 bucket settings");
        }
    }

    private void fallbackForStoringResource(Throwable ex) {
        LOGGER.info("Fallback CircuitBreaker" + ex);
    }

    private void sendDeleteResourceFromStorageRequest(String path, String storageType) throws ResourceAccessException {
        final String url = getServiceInstancesByApplicationName(storageServiceName) + "/storages" + "/" + storageType +
                "?path=" + path;
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.delete(url);
    }

    private String getServiceInstancesByApplicationName(String serviceName) {
        List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceName);
        ServiceInstance serviceInstance = instanceList.get(0);
        return serviceInstance.getUri().toString();
    }

    private void deleteMultipleObjects(String bucketName, List<String> deletedListS3){
        ArrayList<ObjectIdentifier> keys = new ArrayList<>();
        ObjectIdentifier resourceId;
        for (String s : deletedListS3) {
            resourceId = ObjectIdentifier.builder().key(s).build();
            keys.add(resourceId);
        }
        Delete del = Delete.builder()
                .objects(keys)
                .build();
        try {
            DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName).delete(del).build();
            s3Client.deleteObjects(multiObjectDeleteRequest);
        } catch (S3Exception e) {
            System.exit(1);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Please, check S3 bucket settings");
        }
    }
}
