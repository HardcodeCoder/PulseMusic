package com.hardcodecoder.pulsemusic.model;

public class ArtistModel {

    private int mId;
    private String mArtistName;
    private int mNumOfAlbums;
    private int mNumOfTracks;

    public ArtistModel(int mId, String mArtistName, int mNumOfAlbums, int mNumOfTracks) {
        this.mId = mId;
        this.mArtistName = mArtistName;
        this.mNumOfAlbums = mNumOfAlbums;
        this.mNumOfTracks = mNumOfTracks;
    }

    public int getId() {
        return mId;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public int getNumOfAlbumns() {
        return mNumOfAlbums;
    }

    public int getNumOfTracks() {
        return mNumOfTracks;
    }
}
