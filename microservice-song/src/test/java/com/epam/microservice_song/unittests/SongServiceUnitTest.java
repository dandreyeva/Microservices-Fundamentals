package com.epam.microservice_song.unittests;

import com.epam.microservice_song.model.Song;
import com.epam.microservice_song.repo.SongRepository;
import com.epam.microservice_song.service.SongService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@Import({SongService.class})
public class SongServiceUnitTest {

    @Mock
    SongRepository repository;

    @InjectMocks
    private SongService songService;

    @Test
    public void getSongByIdTest() {
        final int songId = 1;
        final String songName = "Diamonds";
        final String songArtist = "Rihanna";
        final String songAlbum = "Diamonds";
        final String songLength = "2:34";
        final String songYear = "2010";

        Song defaultSong = new Song();
        defaultSong.setId(songId);
        defaultSong.setName(songName);
        defaultSong.setArtist(songArtist);
        defaultSong.setAlbum(songAlbum);
        defaultSong.setLength(songLength);
        defaultSong.setYear(songYear);


        Mockito
                .when(this.repository.findById(songId))
                .thenReturn(Optional.of(defaultSong));

        Optional<Song> song = this.songService.getSongById(songId);

        assertThat(song.get())
                .hasFieldOrPropertyWithValue("id", songId)
                .hasFieldOrPropertyWithValue("name", songName)
                .hasFieldOrPropertyWithValue("artist", songArtist)
                .hasFieldOrPropertyWithValue("album", songAlbum)
                .hasFieldOrPropertyWithValue("length", songLength)
                .hasFieldOrPropertyWithValue("year", songYear);
    }
}
