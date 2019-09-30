package com.hardcodecoder.pulsemusic.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.PMS;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.playback.PlaybackManager;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.themes.ThemeManager;
import com.hardcodecoder.pulsemusic.utils.PlaylistStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends Activity {

    private final Handler mHandler = new Handler();
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private TextView startTime;
    private TextView endTime;
    private SeekBar seekBar;
    private final Runnable mUpdateProgressTask = this::updateProgressBar;
    private ImageButton mPlayPause;
    private MediaBrowser mMediaBrowser;
    private MediaController mController;
    private TextView artistAlbums;
    private PlaybackState mState;
    private TextView toolbarSongTitle;
    private int progress = 0;
    private boolean isFav = false;
    private boolean mFavListModified = false;
    private List<MusicModel> favourites = null;
    private ImageView mFavBtn;
    private ImageView mRepeatBtn;
    private TrackManager tm;

    /*
     * Components for seek bar progress update
     */
    private ScheduledFuture<?> mScheduleFuture;

    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            updateMetaData(metadata);
            setFavoriteButtonState();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeManager.getThemeToApply());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_now_playing);

        connectToMediaSession();
        tm = TrackManager.getInstance();
        /*
         * Referencing Views
         */
        startTime = findViewById(R.id.activity_np_start_time);
        endTime = findViewById(R.id.activity_np_end_time);
        seekBar = findViewById(R.id.activity_np_seekBar);
        mPlayPause = findViewById(R.id.activity_np_play_pause_btn);
        artistAlbums = findViewById(R.id.activity_np_album_artist_name);
        toolbarSongTitle = findViewById(R.id.activity_np_song_title);
        mFavBtn = findViewById(R.id.activity_np_favourite_btn);
        mRepeatBtn = findViewById(R.id.activity_np_btn_repeat);

        findViewById(R.id.activity_np_close_btn).setOnClickListener(v -> finishAfterTransition());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                startTime.setText(DateUtils.formatElapsedTime(progress / 4));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progress = seekBar.getProgress();
                mController.getTransportControls().seekTo(progress / 4);
            }
        });
        initButtons();
    }

    private void initButtons() {
        findViewById(R.id.activity_np_skip_next_btn).setOnClickListener(v -> mController.getTransportControls().skipToNext());
        findViewById(R.id.activity_np_skip_prev_btn).setOnClickListener(v -> mController.getTransportControls().skipToPrevious());

        mFavBtn.setOnClickListener(v -> {
            if (isFav) removeFromFavorite();
            else addToFavorite();
        });
        setFavoriteButtonState();
        updateRepeatBtn();
    }

    private void updateRepeatBtn() {
        mRepeatBtn.setImageResource(tm.isCurrentTrackInRepeatMode() ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);

        mRepeatBtn.setOnClickListener(v -> {
            boolean b = tm.isCurrentTrackInRepeatMode();
            mRepeatBtn.setImageResource(b ? R.drawable.ic_repeat : R.drawable.ic_repeat_one);
            tm.repeatCurrentTrack(!b);
            if (b)
                Toast.makeText(this, getString(R.string.repeat_disabled), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.repeat_enabled), Toast.LENGTH_SHORT).show();
        });
    }

    private void setFavoriteButtonState() {
        if (null == favourites) favourites = PlaylistStorageManager.getFavorite(this);
        boolean contains = false;
        if (null != favourites) for (MusicModel md : favourites) {
            if (md.getSongName().equals(TrackManager.getInstance().getActiveQueueItem().getSongName())) {
                contains = true;
                break;
            }
        }
        if (contains) {
            mFavBtn.setImageResource(R.drawable.ic_favorite_active);
            isFav = true;
        } else {
            mFavBtn.setImageResource(R.drawable.ic_favorite);
            isFav = false;
        }
    }

    private void addToFavorite() {
        if (null == favourites)
            favourites = new ArrayList<>();
        favourites.add(TrackManager.getInstance().getActiveQueueItem());
        isFav = true;
        mFavListModified = true;
        mFavBtn.setImageResource(R.drawable.ic_favorite_active);
        Toast.makeText(this, getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();
    }

    private void removeFromFavorite() {
        String title = TrackManager.getInstance().getActiveQueueItem().getSongName();
        for (MusicModel md : favourites) {
            if (md.getSongName().equals(title)) {
                favourites.remove(md);
                break;
            }
        }
        isFav = false;
        mFavListModified = true;
        mFavBtn.setImageResource(R.drawable.ic_favorite);
        Toast.makeText(this, getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();
    }

    private void updateMetaData(MediaMetadata metadata) {
        if (metadata != null) {
            stopSeekBarUpdate();
            GlideApp
                    .with(this)
                    .load(metadata.getBitmap(PlaybackManager.METADATA_ALBUM_ART))
                    .error(R.drawable.np_album_art)
                    .transform(GlideConstantArtifacts.getDefaultRoundingRadius())
                    .into((ImageView) findViewById(R.id.activity_np_album_art));

            long sec = metadata.getLong(PlaybackManager.METADATA_DURATION_KEY) / 1000;
            progress = 0;
            seekBar.setProgress(progress);
            seekBar.setMax((int) sec * 4);
            endTime.setText(DateUtils.formatElapsedTime(sec));

            artistAlbums.setText("");
            String s = metadata.getString(PlaybackManager.METADATA_ARTIST_KEY);
            if (s.length() > 35)
                s = s.substring(0, 36);
            artistAlbums.append("Artist \u25CF ");
            artistAlbums.append(s);

            s = metadata.getString(PlaybackManager.METADATA_ALBUM_KEY);
            if (s.length() > 25)
                s = s.substring(0, 25);
            artistAlbums.append("\nAlbum \u25CF ");
            artistAlbums.append(s);

            toolbarSongTitle.setText(metadata.getText(PlaybackManager.METADATA_TITLE_KEY));
            toolbarSongTitle.setSelected(true);
        }
    }

    private void updatePlaybackState(PlaybackState state) {
        if (state != null) {
            mState = state;
            Drawable d;
            switch (state.getState()) {

                case PlaybackState.STATE_PLAYING:
                    d = getDrawable(R.drawable.avd_play_to_pause);
                    mPlayPause.setImageDrawable(d);
                    if (d instanceof AnimatedVectorDrawable)
                        ((AnimatedVectorDrawable) d).start();
                    scheduleSeekBarUpdate();
                    updateRepeatBtn();
                    break;

                case PlaybackState.STATE_STOPPED:
                    d = getDrawable(R.drawable.avd_pause_to_play);
                    mPlayPause.setImageDrawable(d);
                    if (d instanceof AnimatedVectorDrawable)
                        ((AnimatedVectorDrawable) d).start();
                    stopSeekBarUpdate();
                    progress = 0;
                    seekBar.setProgress(progress);
                    break;

                case PlaybackState.STATE_PAUSED:
                    d = getDrawable(R.drawable.avd_pause_to_play);
                    mPlayPause.setImageDrawable(d);
                    if (d instanceof AnimatedVectorDrawable)
                        ((AnimatedVectorDrawable) d).start();
                    stopSeekBarUpdate();
                    --progress;
                    break;

                case PlaybackState.STATE_BUFFERING:
                case PlaybackState.STATE_CONNECTING:
                case PlaybackState.STATE_ERROR:
                case PlaybackState.STATE_FAST_FORWARDING:
                case PlaybackState.STATE_NONE:
                case PlaybackState.STATE_REWINDING:
                case PlaybackState.STATE_SKIPPING_TO_NEXT:
                case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                    break;
            }
        }
    }

    private void connectToMediaSession() {
        mMediaBrowser = new MediaBrowser(this, new ComponentName(this, PMS.class), new MediaBrowser.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    //Setting Media Controller
                    mController = new MediaController(NowPlayingActivity.this, mMediaBrowser.getSessionToken());
                    setMediaController(mController);
                    mController.registerCallback(mCallback);
                    //mController.sendCommand(PlaybackManager.QUERY_GET_ELAPSED_TIME, null, mResultReceiver);
                    updateMetaData(mController.getMetadata());
                    mState = mController.getPlaybackState();
                    if (null != mState) {
                        long elapsedProgress = mState.getPosition() / 250;
                        progress = (int) elapsedProgress;
                        seekBar.setProgress(progress);

                        //Keep this block here, moving it to onCreate can
                        //create issues if this gets executed before onConnected
                        //onClick listener will not be set due to null state
                        //mState variable gets updated from onPlaybackStateChanged hence
                        //latest state is check before invoking any action
                        mPlayPause.setOnClickListener(v -> {
                            if (mState.getState() == PlaybackState.STATE_PLAYING)
                                mController.getTransportControls().pause();
                            else mController.getTransportControls().play();
                        });
                    }
                    updatePlaybackState(mController.getPlaybackState());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
        mMediaBrowser.connect();
    }

    private void scheduleSeekBarUpdate() {
        if (seekBar.getProgress() == seekBar.getMax())
            seekBar.setProgress(progress = 0);
        if (!mExecutorService.isShutdown()) {
            if (null != mScheduleFuture)
                mScheduleFuture.cancel(true);
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(() -> mHandler.post(mUpdateProgressTask), 0, 250, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekBarUpdate() {
        if (mScheduleFuture != null)
            mScheduleFuture.cancel(true);
        if (seekBar.getProgress() == seekBar.getMax())
            seekBar.setProgress(progress = 0);
    }

    private void updateProgressBar() {
        seekBar.setProgress(++progress);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFavListModified) PlaylistStorageManager.saveFavorite(this, favourites);
    }

    @Override
    protected void onDestroy() {
        mController.unregisterCallback(mCallback);
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown())
            mExecutorService.shutdown();
        mMediaBrowser.disconnect();
        super.onDestroy();
    }
}
