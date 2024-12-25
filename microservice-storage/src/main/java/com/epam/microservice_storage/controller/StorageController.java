package com.epam.microservice_storage.controller;

import com.epam.microservice_storage.dto.StorageRequestDTO;
import com.epam.microservice_storage.model.Storage;
import com.epam.microservice_storage.service.StorageService;
import com.epam.microservice_storage.utils.StorageMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/storages")
public class StorageController {

    private StorageService storageService;
    private StorageMapper storageMapper;

    public StorageController(StorageService storageService, StorageMapper storageMapper) {
        this.storageService = storageService;
        this.storageMapper = storageMapper;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Integer>> createStorage(@RequestBody StorageRequestDTO storageRequest) {
        Map<String, Integer> responseBody;
        if (validateRequest(storageRequest)) {
            int id = storageService.saveStorage(storageMapper.toStorage(storageRequest));
            responseBody = Map.of("id", id);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }
    }

    @GetMapping
    public Iterable<Storage> getStorages() {
        return storageService.getStorageList();
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> removeStorages(@RequestParam String id) {
        String[] idList = id.split(",");
        if (validateId(id)) {
            Map<String, List<Integer>> responseBody = new HashMap<>();
            List<Integer> deletedList = new ArrayList<>();
            for (String s : idList) {
                int intId = Integer.parseInt(s);
                if (storageService.existById(intId)) {
                    storageService.removeStorageById(intId);
                    deletedList.add(intId);
                }
            }
            responseBody.put("id", deletedList);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }
    }

    private boolean validateRequest(StorageRequestDTO storageRequest) {
        return storageRequest.getStorageType() != null && storageRequest.getBucket() != null;
    }

    private boolean validateId(String id) {
        for(int i = 0; i < id.length(); i++){
            if (Character.isLetter(id.charAt(i)))
                return false;
        }
        return id.length() <= 200;
    }
}
