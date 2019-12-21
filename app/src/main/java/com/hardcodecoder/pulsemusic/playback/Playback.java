package com.hardcodecoder.pulsemusic.playback;

import com.hardcodecoder.pulsemusic.model.MusicModel;

public interface Playback {

    void onPlay(MusicModel md, boolean mediaHasChanged);

    void onPause();

    void onSeekTo(long position);

    void onStop(boolean abandonAudioFocus);

    int getActiveMediaId();

    boolean isPlaying();

    long getCurrentStreamingPosition();

    int getPlaybackState();

    void setCallback(Callback callback);

    interface Callback {
        void onPlaybackCompletion();

        void onPlaybackStateChanged(int state);

        void onFocusChanged(boolean resumePlayback);
    }
}
