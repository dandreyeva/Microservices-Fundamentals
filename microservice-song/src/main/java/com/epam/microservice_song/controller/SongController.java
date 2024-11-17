package com.epam.microservice_song.controller;

import com.epam.microservice_song.dto.SongRequestDTO;
import com.epam.microservice_song.dto.SongResponseDTO;
import com.epam.microservice_song.model.Song;
import com.epam.microservice_song.service.SongService;
import com.epam.microservice_song.utils.SongMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
@Validated
public class SongController {

    private SongService songService;
    private SongMapper songMapper;

    public SongController(SongService songService, SongMapper songMapper) {
        this.songService = songService;
        this.songMapper = songMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponseDTO> getSongById(@PathVariable(value = "id") int songId) {
        Song song = songService.getSongById(songId).orElse(null);
        if (song != null) {
            SongResponseDTO songResponseDTO = songMapper.toSongResponseDTO(song);
            return new ResponseEntity<>(songResponseDTO, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Song was not found");
        }
    }

    @PostMapping (produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Integer>> createSong(@RequestBody SongRequestDTO songRequest) {
        Map<String, Integer> responseBody;
        if (validateRequest(songRequest)) {
            int id = songService.createNewSong(songMapper.toSong(songRequest));
            responseBody = Map.of("id", id);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> removeSongs(@RequestParam String id) {
        String[] idList = id.split(",");
        if (validateId(id)) {
            Map<String, List<Integer>> responseBody = new HashMap<>();
            List<Integer> deletedList = new ArrayList<>();
            for (String s : idList) {
                int intId = Integer.parseInt(s);
                if (songService.existById(intId)) {
                    songService.removeSongsById(intId);
                    deletedList.add(intId);
                }
            }
            responseBody.put("id", deletedList);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, check the request");
        }
    }

    private boolean validateId(String id) {
        for(int i = 0; i < id.length(); i++){
            if (Character.isLetter(id.charAt(i)))
                return false;
        }
        return id.length() <= 200;
    }

    private boolean validateRequest(SongRequestDTO songRequest) {
        return songRequest.getYear() != null && songRequest.getName() != null &&
                songRequest.getLength() != null && songRequest.getArtist() != null
                && songRequest.getAlbum() != null;
    }
}
