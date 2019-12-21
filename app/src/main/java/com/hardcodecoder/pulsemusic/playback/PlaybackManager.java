package com.hardcodecoder.pulsemusic.playback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.util.Log;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.io.InputStream;


public class PlaybackManager implements Playback.Callback {

    public static final String METADATA_TITLE_KEY = MediaMetadata.METADATA_KEY_TITLE;
    public static final String METADATA_ALBUM_KEY = MediaMetadata.METADATA_KEY_ALBUM;
    public static final String METADATA_ARTIST_KEY = MediaMetadata.METADATA_KEY_ARTIST;
    public static final String METADATA_DURATION_KEY = MediaMetadata.METADATA_KEY_DURATION;
    public static final String METADATA_ALBUM_ART = MediaMetadata.METADATA_KEY_ALBUM_ART;
    public static final short ACTION_PLAY_NEXT = 1;
    public static final short ACTION_PLAY_PREV = -1;
    private static final String TAG = "PlaybackManager";
    private final PlaybackState.Builder mStateBuilder = new PlaybackState.Builder();
    private final MediaMetadata.Builder mMetadataBuilder = new MediaMetadata.Builder();
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private TrackManager mTrackManager;
    private Context mContext;
    private final MediaSession.Callback mMediaSessionCallback = new MediaSession.Callback() {

        @Override
        public void onPlay() {
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            handlePauseRequest();
        }

        @Override
        public void onSkipToNext() {
            handleSkipRequest(ACTION_PLAY_NEXT);
        }

        @Override
        public void onSkipToPrevious() {
            handleSkipRequest(ACTION_PLAY_PREV);
        }

        @Override
        public void onStop() {
            handleStopRequest();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.onSeekTo(pos);
        }
    };

    public PlaybackManager(Context context, Playback playback, TrackManager trackManager, PlaybackServiceCallback serviceCallback) {
        this.mContext = context;
        this.mPlayback = playback;
        this.mTrackManager = trackManager;
        this.mServiceCallback = serviceCallback;
        mPlayback.setCallback(this);
    }

    public MediaSession.Callback getSessionCallbacks() {
        return mMediaSessionCallback;
    }

    private void handlePlayRequest() {
        boolean hasMediaChanged = false;
        MusicModel md = mTrackManager.getActiveQueueItem();
        if (mPlayback.getActiveMediaId() != md.getId())
            hasMediaChanged = true;
        mServiceCallback.onPlaybackStart();
        updateMetaData(hasMediaChanged);
        mPlayback.onPlay(md, hasMediaChanged);
    }

    private void handlePauseRequest() {
        mServiceCallback.onPlaybackStopped();
        mPlayback.onPause();
    }

    private void handleStopRequest() {
        mServiceCallback.onPlaybackStopped();
        mPlayback.onStop(true);
    }

    private void handleSkipRequest(short di) {
        if (mTrackManager.canSkipTrack(di))
            handlePlayRequest();
        else
            handlePauseRequest();
    }

    private void updatePlaybackState(int currentState) {
        mStateBuilder.setState(currentState, mPlayback.getCurrentStreamingPosition(), currentState == PlaybackState.STATE_PLAYING ? 1 : 0);
        mStateBuilder.setActions(getActions(currentState));
        mServiceCallback.onPlaybackStateChanged(mStateBuilder.build());

        if (currentState == PlaybackState.STATE_PLAYING) {
            mServiceCallback.onStartNotification();
        } else if (currentState == PlaybackState.STATE_STOPPED) {
            mServiceCallback.onStopNotification();
        }
    }

    private void updateMetaData(boolean b) {
        if (b) {
            MusicModel md = mTrackManager.getActiveQueueItem();
            mMetadataBuilder.putLong(METADATA_DURATION_KEY, md.getDuration());
            mMetadataBuilder.putString(METADATA_TITLE_KEY, md.getSongName());
            mMetadataBuilder.putString(METADATA_ARTIST_KEY, md.getArtist());
            mMetadataBuilder.putString(METADATA_ALBUM_KEY, md.getAlbum());
            mMetadataBuilder.putBitmap(METADATA_ALBUM_ART, loadAlbumArt(md.getAlbumArtUrl()));
            mServiceCallback.onMetaDataChanged(mMetadataBuilder.build());
        }
    }

    private Bitmap loadAlbumArt(String path) {
        try {
            Uri uri = Uri.parse(path);
            InputStream is = mContext.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.w(TAG, "Album art not found");
            return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.album_art_error);
        }
    }

    private long getActions(int state) {
        long actions;
        if (state == PlaybackState.STATE_PLAYING) {
            actions = PlaybackState.ACTION_PAUSE;
        } else {
            actions = PlaybackState.ACTION_PLAY;
        }
        return PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SKIP_TO_NEXT | actions;
    }

    @Override
    public void onFocusChanged(boolean resumePlayback) {
        if (resumePlayback)
            handlePlayRequest();
        else
            handlePauseRequest();
    }

    @Override
    public void onPlaybackCompletion() {
        handleSkipRequest(ACTION_PLAY_NEXT);
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        updatePlaybackState(state);
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onPlaybackStopped();

        void onStartNotification();

        void onStopNotification();

        void onPlaybackStateChanged(PlaybackState newState);

        void onMetaDataChanged(MediaMetadata newMetaData);
    }
}
