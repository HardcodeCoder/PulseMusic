package com.hardcodecoder.pulsemusic.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.io.IOException;

public class LocalPlayback implements
        Playback,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {

    //private static final String TAG = "LocalPlayback";
    private final IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private Context mContext;
    private AudioManager mAudioManager;
    private Playback.Callback mPlaybackCallback;
    private MediaPlayer mp;
    private int resumePosition;
    private boolean isBecomingNoisyReceiverRegistered = false;
    private int mCurrentState;
    private TelephonyManager mTelephonyManager;
    private AudioFocusRequest mAudioFocusRequest = null;
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mPlaybackCallback.onFocusChanged(false);
            }
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private TrackManager mTrackManager;
    private int mMediaId = -99;
    private Handler mHandler;
    private boolean mDelayedPlayback = false;
    private int mPlaybackState = PlaybackState.STATE_NONE;

    public LocalPlayback(Context context, TrackManager trackManager, Handler handler) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mTrackManager = trackManager;
        this.mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        this.mHandler = handler;
        if (mHandler == null) {
            mHandler = new Handler(Looper.myLooper());
        }
    }

    @Override
    public void setCallback(Callback callback) {
        mPlaybackCallback = callback;
    }

    private void initMediaPlayer() {
        mp = new MediaPlayer();
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.reset();
        try {
            mp.setDataSource(mTrackManager.getActiveQueueItem().getSongPath());
            mp.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            //Stop playback if data source failed
            Toast.makeText(mContext, "Music file not found, playing next song in queue", Toast.LENGTH_LONG).show();
            mPlaybackCallback.onPlaybackCompletion();
        }
        mTrackManager.addToHistory();
    }

    @Override
    public void onPlay(MusicModel md, boolean mediaHasChanged) {
        if (tryGetAudioFocus()) {
            mDelayedPlayback = false;
            if (!mediaHasChanged) {
                if (mp != null && resumePosition != -1) {
                    //if (mp.getCurrentPosition() <= resumePosition || mp.getCurrentPosition() >= resumePosition)
                    //mp.seekTo(resumePosition);

                    //mp.start();
                    play();
                } else if (null == mp) {
                    initMediaPlayer();
                }
            } else {
                onStop(false);
                initMediaPlayer();
                mMediaId = md.getId();
            }
            registerBecomingNoisyReceiver();
            callStateListener();
        } else if (mDelayedPlayback) mMediaId = md.getId();
    }

    private void play() {
        mp.start();
        mPlaybackState = PlaybackState.STATE_PLAYING;
        mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //mp.start();
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onStop(false);
        mPlaybackCallback.onPlaybackCompletion();
    }

    @Override
    public void onPause() {
        if (mp != null) {
            mp.pause();
            resumePosition = mp.getCurrentPosition() - 250;
        }
        mPlaybackState = PlaybackState.STATE_PAUSED;
        mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
    }

    @Override
    public void onSeekTo(long position) {
        if (mp != null) {
            mp.seekTo((int) position * 1000);
            mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
        }
    }

    @Override
    public void onStop(boolean abandonAudioFocus) {
        if (abandonAudioFocus) abandonAudioFocus();

        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
        if (isBecomingNoisyReceiverRegistered) {
            mContext.unregisterReceiver(becomingNoisyReceiver);
            isBecomingNoisyReceiverRegistered = false;
        }
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        if (abandonAudioFocus) {
            mPlaybackState = PlaybackState.STATE_STOPPED;
            mPlaybackCallback.onPlaybackStateChanged(mPlaybackState);
        }
    }

    @Override
    public int getActiveMediaId() {
        return mMediaId;
    }

    @Override
    public boolean isPlaying() {
        if (null != mp)
            return mp.isPlaying();
        return false;
    }

    @Override
    public long getCurrentStreamingPosition() {
        if (null == mp) return 0;
        return mp.getCurrentPosition();
    }

    @Override
    public int getPlaybackState() {
        return mPlaybackState;
    }

    private boolean tryGetAudioFocus() {
        int r;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (null == mAudioFocusRequest) {
                AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(mPlaybackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(LocalPlayback.this, mHandler)
                        .build();
            }
            r = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            if (r == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) mDelayedPlayback = true;
        } else {
            r = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioFocusRequest != null)
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        else {
            mAudioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mCurrentState = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                mCurrentState = AudioManager.AUDIOFOCUS_LOSS;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mCurrentState = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                mCurrentState = AudioManager.AUDIOFOCUS_GAIN;
                break;
        }
        if (mp != null) configurePlayerState();
        else if (mDelayedPlayback) mPlaybackCallback.onFocusChanged(true);
    }

    private void configurePlayerState() {
        if (mCurrentState == AudioManager.AUDIOFOCUS_LOSS || mCurrentState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            mPlaybackCallback.onFocusChanged(false);
        } else if (mCurrentState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            mp.setVolume(0.2f, 0.2f);
        else if (mCurrentState == AudioManager.AUDIOFOCUS_GAIN) {
            mp.setVolume(1.0f, 1.0f);
            mPlaybackCallback.onFocusChanged(true);
        }
    }

    private void registerBecomingNoisyReceiver() {
        //Register after getting audio focus
        if (!isBecomingNoisyReceiverRegistered) {
            mContext.registerReceiver(becomingNoisyReceiver, filter);
            isBecomingNoisyReceiverRegistered = true;
        }
    }

    private void callStateListener() {
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        mPhoneStateListener = new PhoneStateListener() {
            boolean wasRinging = false;
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    //case TelephonyManager.CALL_STATE_OFFHOOK:
                    //break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        wasRinging = true;
                        mPlaybackCallback.onFocusChanged(false);
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        // this should be the last piece of code before the break
                        if (wasRinging) mPlaybackCallback.onFocusChanged(true);
                        wasRinging = false;
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void saveRecentTrack() {
        mTrackManager.saveTracks(mContext);
    }
}
