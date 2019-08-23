package com.hardcodecoder.pulsemusic.interfaces;


import com.hardcodecoder.pulsemusic.model.AlbumModel;

import java.util.List;

public interface AlbumDataFetchCompletionCallback {

    void onTaskComplete(List<AlbumModel> data);
}
