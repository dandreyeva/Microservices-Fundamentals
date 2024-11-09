package com.epam.microservice_song.service;

import com.epam.microservice_song.model.Song;
import com.epam.microservice_song.repo.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SongService {

    private SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public int createNewSong(Song song) {
        return songRepository.save(song).getId();
    }

    public Optional<Song> getSongById(int id) {
        return songRepository.findById(id);
    }

    public void removeSongsById(int id) {
        songRepository.deleteById(id);
    }

    public boolean existById(int id) {
        return songRepository.existsById(id);
    }
}
