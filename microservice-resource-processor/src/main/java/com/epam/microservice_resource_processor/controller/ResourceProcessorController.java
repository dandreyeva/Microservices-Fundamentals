package com.epam.microservice_resource_processor.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.epam.microservice_resource_processor.utils.ResourceMetadata.*;

@RestController
public class ResourceProcessorController {
    @Value("${spring.application.song.service.name}")
    private String songServiceName;
    private RestTemplate restTemplate;

    public ResourceProcessorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private void sendSongPostRequest(Map<String, String> metadataMap, int id) throws ResourceAccessException {
        final var url = getServiceInstancesByApplicationName(songServiceName) + "/songs";
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("name", metadataMap.get(SONG_TITLE));
        body.put("artist", metadataMap.get(SONG_ARTIST));
        body.put("album", metadataMap.get(SONG_ALBUM));
        body.put("length", metadataMap.get(SONG_LENGTH));
        body.put("year", metadataMap.get(SONG_YEAR));
        body.put("resourceId", String.valueOf(id));

        final var request = new HttpEntity<>(body, headers);
        restTemplate.postForObject(url, request, String.class);
    }

    private void sendSongDeleteRequest(String id) throws ResourceAccessException {
        final var url = getServiceInstancesByApplicationName(songServiceName) + "/songs" + "?id=" +id;
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.delete(url);
    }

    private String getServiceInstancesByApplicationName(String serviceName) {
        //var instanceList = discoveryClient.getInstances(serviceName);
       // var serviceInstance = instanceList.get(new Random().nextInt(instanceList.size()));
        return null;//serviceInstance.getUri().toString();
    }

    private boolean validateMetadata(Map<String, String> metadataMap) {
        return metadataMap.get(SONG_TITLE) != null && metadataMap.get(SONG_ARTIST) != null
               && metadataMap.get(SONG_ALBUM) != null && metadataMap.get(SONG_LENGTH) != null
                && metadataMap.get(SONG_YEAR) != null;
    }
}
