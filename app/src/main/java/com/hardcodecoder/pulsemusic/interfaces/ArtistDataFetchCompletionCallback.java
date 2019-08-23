package com.hardcodecoder.pulsemusic.interfaces;

import com.hardcodecoder.pulsemusic.model.ArtistModel;

import java.util.List;

public interface ArtistDataFetchCompletionCallback {

    void onTaskComplete(List<ArtistModel> data);
}
