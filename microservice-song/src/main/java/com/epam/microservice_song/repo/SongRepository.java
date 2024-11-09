package com.epam.microservice_song.repo;

import com.epam.microservice_song.model.Song;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends CrudRepository<Song, Integer> {
}
