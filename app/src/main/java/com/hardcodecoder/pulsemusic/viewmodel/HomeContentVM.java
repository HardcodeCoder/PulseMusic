package com.hardcodecoder.pulsemusic.viewmodel;


import com.hardcodecoder.pulsemusic.model.MusicModel;
import java.util.List;

public class HomeContentVM {

    private static final HomeContentVM singleInstance = new HomeContentVM();
    private List<MusicModel> mYourMayLikeList;
    private List<MusicModel> mRecentArtist;
    private List<MusicModel> mNewInLibrary;

    public static HomeContentVM getInstance(){
        return singleInstance;
    }


    public List<MusicModel> getYourMayLikeList() {
        return mYourMayLikeList;
    }

    public void setYourMayLikeList(List<MusicModel> mYourMayLikeList) {
        this.mYourMayLikeList = mYourMayLikeList;
    }

    public List<MusicModel> getRecentArtist() {
        return mRecentArtist;
    }

    public void setRecentArtist(List<MusicModel> mRecentArtist) {
        this.mRecentArtist = mRecentArtist;
    }

    public List<MusicModel> getNewInLibrary() {
        return mNewInLibrary;
    }

    public void setNewInLibrary(List<MusicModel> mNewInLibrary) {
        this.mNewInLibrary = mNewInLibrary;
    }
}
