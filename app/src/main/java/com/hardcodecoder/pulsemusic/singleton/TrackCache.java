package com.hardcodecoder.pulsemusic.singleton;

import androidx.annotation.Nullable;

import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class TrackCache {

    private static final TrackCache ourInstance = new TrackCache();

    public static TrackCache getInstance() {
        return ourInstance;
    }

    private TrackCache() {
    }

    private List<MusicModel> recentlyAdded = null;
    private List<MusicModel> pickedTracks = null;
    private boolean holdingNewItem = false;

    public void cacheRecentlyAdded(List<MusicModel> data) {
        recentlyAdded = data;
    }

    @Nullable
    public List<MusicModel> recentlyAddedTracks() {
        return recentlyAdded;
    }

    /*
     * Helper method to temporarily store tracks picked from {@Link TrackPickerActivity}
     * or HomeFragment#shuffled track
     * and pass it to the caller calling getTracks
     * this combination of method can be used only once
     * and after tracks are given back further calls to giveTrack returns null
     */
    public void holdPickedTracks(List<MusicModel> list) {
        pickedTracks = list;
        holdingNewItem = true;
    }

    public List<MusicModel> giveTracks() {
        if (holdingNewItem) holdingNewItem = false;
        else pickedTracks = null;
        return pickedTracks;
    }
}
