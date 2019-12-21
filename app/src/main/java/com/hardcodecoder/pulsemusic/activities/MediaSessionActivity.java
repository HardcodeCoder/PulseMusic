package com.hardcodecoder.pulsemusic.activities;

import android.content.ComponentName;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hardcodecoder.pulsemusic.PMS;

public abstract class MediaSessionActivity extends PMBActivity {

    private MediaBrowser mMediaBrowser = null;
    private MediaController mController = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       connectToMediaSession();
    }

    public void connectToMediaSession() {
        mMediaBrowser = new MediaBrowser(this, new ComponentName(MediaSessionActivity.this, PMS.class),
                // Which MediaBrowserService
                new MediaBrowser.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            // Ah, here’s our Token again
                            MediaSession.Token token = mMediaBrowser.getSessionToken();

                            // This is what gives us access to everything
                            mController = new MediaController(MediaSessionActivity.this, token);

                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            setMediaController(mController);

                            onMediaServiceConnected(mController);
                        } catch (Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                }, null); // optional Bundle
        mMediaBrowser.connect();
    }

    public void disconnectFromMediaSession() {
        if (null != mMediaBrowser)
            mMediaBrowser.disconnect();
    }

    public MediaBrowser getMediaBrowser() {
        return mMediaBrowser;
    }

    public void playMedia() {
        if(null == mController)
            mController = getMediaController();
        mController.getTransportControls().play();
    }

    @Override
    protected void onDestroy() {
        disconnectFromMediaSession();
        super.onDestroy();
    }

    public abstract void onMediaServiceConnected(MediaController controller);
}
