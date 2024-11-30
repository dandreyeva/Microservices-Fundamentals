package com.epam.microservice_song.integrationtests;

import com.epam.microservice_song.model.Song;
import com.epam.microservice_song.repo.SongRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("songintegrationtest")
public class SongIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SongRepository repository;

    @Test
    public  void songIntegrationTest() throws Exception{
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

        this.repository.save(defaultSong);

        final String link = "/songs/" + defaultSong.getId();

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(link)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(defaultSong.getName())))
                .andExpect(jsonPath("$.artist", is(defaultSong.getArtist())))
                .andExpect(jsonPath("$.album", is(defaultSong.getAlbum())))
                .andExpect(jsonPath("$.length", is(defaultSong.getLength())))
                .andExpect(jsonPath("$.year", is(defaultSong.getYear())));
    }
}
