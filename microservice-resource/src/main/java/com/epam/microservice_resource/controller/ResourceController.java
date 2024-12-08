package com.epam.microservice_resource.controller;

import com.epam.microservice_resource.model.Resource;
import com.epam.microservice_resource.service.MessageService;
import com.epam.microservice_resource.service.ResourceService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    @Value("${bucket.name}")
    private String bucketName;
    private MessageService messageService;


    public ResourceController(ResourceService resourceService,
                              S3Client s3Client, MessageService messageService) {
        this.resourceService = resourceService;
        this.s3Client = s3Client;
        this.messageService = messageService;
    }

    @PostMapping(consumes = "audio/mpeg", produces = "application/json")
    public ResponseEntity<Map<String, String>> createResources(@RequestBody byte[] audioData,
                                                                @RequestHeader("fileName") String keyName) {
        var tika = new Tika();
        var type = tika.detect(audioData);//check valid and type of data

        if (keyName != null && type.equals("audio/mpeg")) {
            try {
                PutObjectResponse response = s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(keyName)
                                .build(),
                        software.amazon.awssdk.core.sync.RequestBody.fromBytes(audioData));
                response.responseMetadata();
            } catch (S3Exception e) {
                System.exit(1);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Please, check S3 bucket settings");
            }

            Map<String, String> responseBody;
            var resource = new Resource();
            resource.setName(keyName);
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

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> removeResourcesById(@RequestParam String id) {
        var idList = id.split(",");
        if (validateId(id)) {
            Map<String, List<Integer>> responseBody = new HashMap<>();
            List<Integer> deletedList = new ArrayList<>();
            List<String> deletedListS3 = new ArrayList<>();
            for (var s : idList) {
                var intId = Integer.valueOf(s);
                if (resourceService.existById(intId)) {
                    var resourceName = resourceService.getResourceById(intId).get().getName();
                    deletedListS3.add(resourceName);
                    resourceService.removeResourceById(intId);
                    deletedList.add(intId);
                }
            }
            deleteMultipleObjects(bucketName, deletedListS3);
            responseBody.put("id", deletedList);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }
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

    private boolean validateId(String id) {
        for(var i = 0; i < id.length(); i++){
            if (Character.isLetter(id.charAt(i)))
                return false;
        }
        return id.length() <= 200;
    }
}
