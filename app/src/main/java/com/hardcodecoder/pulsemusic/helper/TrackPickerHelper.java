package com.hardcodecoder.pulsemusic.helper;

import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class TrackPickerHelper {

    private static final TrackPickerHelper ourInstance = new TrackPickerHelper();

    public static TrackPickerHelper getInstance() {
        return ourInstance;
    }

    private TrackPickerHelper() {
    }

    private List<MusicModel> pickedTracks = null;
    private boolean holdingNewItem = false;

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
