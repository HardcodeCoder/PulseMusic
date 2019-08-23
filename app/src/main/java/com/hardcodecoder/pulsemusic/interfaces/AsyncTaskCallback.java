package com.hardcodecoder.pulsemusic.interfaces;

import com.hardcodecoder.pulsemusic.model.AlbumModel;
import com.hardcodecoder.pulsemusic.model.ArtistModel;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public interface AsyncTaskCallback {

    interface Simple {
        void onTaskComplete(List<MusicModel> list);
    }

    interface UpdateCompletion {
        void onUpdateComplete(List<MusicModel> list, boolean isEdited);
    }

    interface AlbumTask {
        void onTaskComplete(List<AlbumModel> data);
    }

    interface ArtistTask {
        void onTaskComplete(List<ArtistModel> data);
    }
}
