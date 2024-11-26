package com.epam.microservice_resource_processor.controller;

import com.epam.microservice_resource_processor.utils.Mp3Parse;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static com.epam.microservice_resource_processor.utils.ResourceMetadata.*;

@RestController
public class ResourceProcessorController {
    private RestTemplate restTemplate;
    private Mp3Parse mp3Parse = new Mp3Parse();
    private RetryTemplate retryTemplate;
    private int counter_resource = 0;
    private int counter_song = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProcessorController.class);

    public ResourceProcessorController(RestTemplate restTemplate, RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
    }
    @RabbitListener(queues = ("resourceIdQueue"))
    public void receiveProduct(String message) {
        byte[] resource;
        try {
            resource = retryTemplate.execute(
                    context -> sendResourceGetRequest(Integer.parseInt(message)));

        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Resource service is unavailable");
        }
        var tika = new Tika();
        String type = tika.detect(resource);//check valid and type of data
        Map<String, String> metadataMap = mp3Parse.parseMP3(new ByteArrayInputStream(resource));
        if (type.equals("audio/mpeg") && validateMetadata(metadataMap)) {
            try {
                retryTemplate.execute(context -> {
                            sendSongPostRequest(metadataMap, Integer.parseInt(message));
                            return true;});
            } catch (ResourceAccessException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Song service is unavailable");
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please, check the request");
        }
    }

    private byte[] sendResourceGetRequest(int id) throws ResourceAccessException {
        counter_resource++;
        LOGGER.info("Sending resource request ... " + "attempt " + counter_resource);
        var url = "http://resource-app:9090/resources/" + id;
        return restTemplate.getForObject(url, byte[].class);
    }

    private void sendSongPostRequest(Map<String, String> metadataMap, int id) throws ResourceAccessException {
        counter_song++;
        LOGGER.info("Sending song request ... " + "attempt " + counter_song);
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
        restTemplate.postForObject("http://song-app:8080/songs", request, String.class);
        LOGGER.info("Song request has been sent");
    }

    /*private void sendSongDeleteRequest(String id) throws ResourceAccessException {
        final var url = songServiceUrl + "?id=" +id;
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.delete(url);
    }*/

    private boolean validateMetadata(Map<String, String> metadataMap) {
        return metadataMap.get(SONG_TITLE) != null && metadataMap.get(SONG_ARTIST) != null
               && metadataMap.get(SONG_ALBUM) != null && metadataMap.get(SONG_LENGTH) != null
                && metadataMap.get(SONG_YEAR) != null;
    }
}