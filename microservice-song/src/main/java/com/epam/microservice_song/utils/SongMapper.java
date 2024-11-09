package com.epam.microservice_song.utils;

import com.epam.microservice_song.dto.SongRequestDTO;
import com.epam.microservice_song.dto.SongResponseDTO;
import com.epam.microservice_song.model.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    public Song toSong(SongRequestDTO songRequestDTO) {
        Song song  = new Song();
        song.setAlbum(songRequestDTO.getAlbum());
        song.setArtist(songRequestDTO.getArtist());
        song.setName(songRequestDTO.getName());
        song.setLength(songRequestDTO.getLength());
        song.setYear(songRequestDTO.getYear());
        song.setResourceId(songRequestDTO.getResourceId());
        return song;
    }

    public SongResponseDTO toSongResponseDTO(Song song) {
        SongResponseDTO responseDTO = new SongResponseDTO();
        responseDTO.setName(song.getName());
        responseDTO.setArtist(song.getArtist());
        responseDTO.setAlbum(song.getAlbum());
        responseDTO.setLength(song.getLength());
        responseDTO.setYear(song.getYear());
        responseDTO.setResourceId(song.getResourceId());
        return responseDTO;
    }
}
