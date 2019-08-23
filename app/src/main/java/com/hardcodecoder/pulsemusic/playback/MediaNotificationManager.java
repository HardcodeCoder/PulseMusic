package com.hardcodecoder.pulsemusic.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.hardcodecoder.pulsemusic.PMS;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import static com.hardcodecoder.pulsemusic.playback.PlaybackManager.METADATA_ALBUM_ART;

public class MediaNotificationManager extends BroadcastReceiver {

    public static final String TAG = "MediaNotificationMan";

    private static final String ACTION_PAUSE = "com.hardcodeCoder.playback.pause";
    private static final String ACTION_PLAY = "com.hardcodeCoder.playback.play";
    private static final String ACTION_PREV = "com.hardcodeCoder.playback.prev";
    private static final String ACTION_NEXT = "com.hardcodeCoder.playback.next";
    private static final String ACTION_STOP = "com.hardcodeCoder.playback.delete";

    private static final String CHANNEL_ID = "com.hardcodecoder.pulsemusic.MUSIC_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    private final NotificationManager mNotificationManager;
    private PendingIntent mPlayIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mStopIntent;
    private PendingIntent pi;
    private PMS mService;
    private MediaSession.Token mSessionToken;
    private MediaController mController;
    private MediaController.TransportControls mTransportControls;
    public boolean mStarted;
    private boolean mInitialised = false;
    private PlaybackState mPlaybackState;
    private MediaMetadata mMetadata;
    private final MediaController.Callback mCb = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            if (null != state) {
                int mState = state.getState();
                if (mPlaybackState.getState() != mState && mState != PlaybackState.STATE_STOPPED) {
                    mPlaybackState = state;
                    updateNotification();
                }
                mPlaybackState = state;
            }
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            if (null != metadata) {
                mMetadata = metadata;
                updateNotification();
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            updateSessionToken();
            try {
                updateSessionToken();
            } catch (Exception e) {
                Log.e(TAG, "could not connect media controller", e);
            }
        }
    };

    public MediaNotificationManager(PMS service) {
        this.mService = service;
        updateSessionToken();
        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    private void updateSessionToken() {
        MediaSession.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaController(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mCb);
                }
            }
        }
    }

    private void updateNotification() {
        Notification notification = createNotification();
        if (mPlaybackState.getState() == PlaybackState.STATE_PLAYING) {
            mService.startForeground(NOTIFICATION_ID, notification);
        } else if (mPlaybackState.getState() == PlaybackState.STATE_PAUSED) {
            mService.stopForeground(false);
        }
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void initialisePendingIntents() {
        String pkg = mService.getPackageName();
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        if (mNotificationManager != null) mNotificationManager.cancelAll();
        pi = PendingIntent.getActivity(mService, REQUEST_CODE, new Intent(mService, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mInitialised = true;
    }

    public void startNotification() {
        if (!mInitialised) {
            initialisePendingIntents();
        }
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();
            // The notification must be updated after setting started to true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel();
            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP);
                mService.registerReceiver(this, filter);
                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    mService.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Notification createNotification() {
        MusicModel md = TrackManager.getInstance().getActiveQueueItem();
        if (md != null) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
            notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(MediaSessionCompat.Token.fromToken(mService.getSessionToken()))
                    .setCancelButtonIntent(mStopIntent)
                    .setShowCancelButton(true)
                    .setShowActionsInCompactView(0, 1, 2))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(md.getSongName())
                    .setContentText(md.getArtist())
                    .setLargeIcon(mMetadata.getBitmap(METADATA_ALBUM_ART))
                    .addAction(R.drawable.ic_round_skip_previous_noti, "Skip prev", mPreviousIntent)
                    .addAction(setButton(), "Play Pause Button", getActionIntent())
                    .addAction(R.drawable.ic_round_skip_next_noti, "Skip Next", mNextIntent)
                    .addAction(R.drawable.ic_close_noti, "Stop Self", mStopIntent)
                    .setContentIntent(pi)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setDeleteIntent(mStopIntent);
            //mStarted = true;
            return notificationBuilder.build();
        }
        return null;
    }

    private int setButton() {
        return mPlaybackState.getState() == PlaybackState.STATE_PLAYING ?
                R.drawable.ic_round_pause_noti : R.drawable.ic_round_play_noti;
    }

    private PendingIntent getActionIntent() {
        return mPlaybackState.getState() == PlaybackState.STATE_PLAYING ?
                mPauseIntent : mPlayIntent;
    }

    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && mTransportControls != null) {
            switch (action) {
                case ACTION_PLAY:
                    mTransportControls.play();
                    break;
                case ACTION_PAUSE:
                    mTransportControls.pause();
                    break;
                case ACTION_STOP:
                    mTransportControls.stop();
                    break;
                case ACTION_NEXT:
                    mTransportControls.skipToNext();
                    break;
                case ACTION_PREV:
                    mTransportControls.skipToPrevious();
                    break;
            }
        }
    }
}
