package com.hardcodecoder.pulsemusic.model;

public class ItemsModel {

    int duration;
    long albumId;
    private String title, path;

    public ItemsModel(String title, String path, int duration, long albumId) {
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public int getDuration() {
        return duration;
    }

    public long getAlbumId() {
        return albumId;
    }
}
