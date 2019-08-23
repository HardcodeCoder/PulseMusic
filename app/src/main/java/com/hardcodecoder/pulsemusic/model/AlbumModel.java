package com.hardcodecoder.pulsemusic.model;

public class AlbumModel {

    private int mId;
    private String mAlbumName;
    private int mSongsCount;
    private String mAlbumArt;

    public AlbumModel(int mId, String mAlbumName, int mSongsCount, String mAlbumArt) {
        this.mId = mId;
        this.mAlbumName = mAlbumName;
        this.mSongsCount = mSongsCount;
        this.mAlbumArt = mAlbumArt;
    }

    public int getId() {
        return mId;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public int getSongsCount() {
        return mSongsCount;
    }

    public String getAlbumArt() {
        return mAlbumArt;
    }
}
