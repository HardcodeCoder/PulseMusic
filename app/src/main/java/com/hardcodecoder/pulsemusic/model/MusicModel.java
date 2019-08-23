package com.hardcodecoder.pulsemusic.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class MusicModel implements Serializable {

    private String songName, artist, songPath, album, albumArtUrl;
    private int duration, modelId;
    private long albumId;

    public MusicModel(int id,
                      String songName,
                      @Nullable String artist,
                      @NonNull String songPath,
                      @Nullable String album,
                      long albumId,
                      @Nullable String albumArtUrl,
                      int duration) {

        this.songName = songName;
        this.artist = artist;
        this.songPath = songPath;
        this.album = album;
        this.albumId = albumId;
        this.albumArtUrl = albumArtUrl;
        this.duration = duration;
        this.modelId = id;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public void setTitle(String title) {
        this.songName = title;
    }

    public int getId() {
        return modelId;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        return artist;
    }

    public String getSongPath() {
        return songPath;
    }

    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public int getDuration() {
        return duration;
    }
}
