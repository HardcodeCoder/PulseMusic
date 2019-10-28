package com.hardcodecoder.pulsemusic.viewmodel;

import com.hardcodecoder.pulsemusic.model.AlbumModel;
import com.hardcodecoder.pulsemusic.model.ArtistModel;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import java.util.List;

public class HomeContentVM {

    private static final HomeContentVM singleInstance = new HomeContentVM();
    private List<MusicModel> mYourMayLikeList;
    private List<MusicModel> mNewInLibrary;
    private List<AlbumModel> mAlbumsList;
    private List<ArtistModel> mArtistList;

    public static HomeContentVM getInstance(){
        return singleInstance;
    }


    public List<MusicModel> getYourMayLikeList() {
        return mYourMayLikeList;
    }

    public void setYourMayLikeList(List<MusicModel> mYourMayLikeList) {
        this.mYourMayLikeList = mYourMayLikeList;
    }

    public List<MusicModel> getNewInLibrary() {
        return mNewInLibrary;
    }

    public void setNewInLibrary(List<MusicModel> mNewInLibrary) {
        this.mNewInLibrary = mNewInLibrary;
    }

    public List<AlbumModel> getAlbumsList() {
        return mAlbumsList;
    }

    public void setAlbumsList(List<AlbumModel> mAlbumsList) {
        this.mAlbumsList = mAlbumsList;
    }

    public List<ArtistModel> getArtistList() {
        return mArtistList;
    }

    public void setArtistList(List<ArtistModel> mArtistList) {
        this.mArtistList = mArtistList;
    }
}
