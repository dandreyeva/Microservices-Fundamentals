package com.epam.microservice_song.dto;

import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

@Validated
public class SongRequestDTO {

    @NonNull
    private String name;
    @NonNull
    private String artist;
    @NonNull
    private String album;
    @NonNull
    private String length;
    @NonNull
    private String year;
    @NonNull
    private String resourceId;

    public SongRequestDTO(@NonNull String name, @NonNull String artist,
                          @NonNull String album, @NonNull String length,
                          @NonNull String year, @NonNull String resourceId) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.year = year;
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getLength() {
        return length;
    }

    public String getYear() {
        return year;
    }

    public String getResourceId() {
        return resourceId;
    }
}
