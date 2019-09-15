package com.hardcodecoder.pulsemusic.ui;

import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.NowPlayingActivity;
import com.hardcodecoder.pulsemusic.playback.PlaybackManager;

public class ControlsFragment extends Fragment {

    private TextView tv1;
    private ImageButton playPause;
    private MediaController mController;
    private MediaController.TransportControls mTransportControl;
    private MediaMetadata mMetadata;
    private PlaybackState mState;

    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            mState = state;
            updateControls();
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            mMetadata = metadata;
            updateMetadata();
        }
    };

    public ControlsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    @Override
    public void onStart() {
        updateController();
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        //iv = v.findViewById(R.id.cf_iv);
        tv1 = v.findViewById(R.id.song_name);
        tv1.setSelected(true);
        playPause = v.findViewById(R.id.cf_play_pause_btn);
        ImageButton skipNext = v.findViewById(R.id.cf_skip_next_btn);
        ImageButton skipPrev = v.findViewById(R.id.cf_skip_prev_btn);

        playPause.setOnClickListener(v1 -> {
            if (mState.getState() == PlaybackState.STATE_PLAYING)
                mTransportControl.pause();
            else
                mTransportControl.play();
        });

        skipNext.setOnClickListener(v1 -> mTransportControl.skipToNext());

        skipPrev.setOnClickListener(v1 -> mTransportControl.skipToPrevious());

        v.setOnClickListener(v1 -> {
            // a potentially time consuming task
            Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
            startActivity(intent);
        });
    }

    private void updateControls() {
        if (null == mState)
            return;
        if (mState.getState() == PlaybackState.STATE_PLAYING)
            playPause.setImageResource(R.drawable.ic_round_pause);
        else
            playPause.setImageResource(R.drawable.ic_round_play);
    }

    private void updateMetadata() {
        if (mMetadata != null) {
            /*GlideApp
                    .with(iv.getContext())
                    .load(mMetadata.getBitmap(PlaybackManager.METADATA_ALBUM_ART))
                    .error(R.drawable.album_art_error)
                    .transform(new RoundedCorners(12))
                    .transition(GenericTransitionOptions.with(R.anim.fade_in_image))
                    .into(iv);*/
            tv1.setText(mMetadata.getText(PlaybackManager.METADATA_TITLE_KEY));
        }
    }

    private void updateController() {
        if (mController == null && getActivity() != null)
            mController = getActivity().getMediaController();
        if (mController != null) {
            mController.registerCallback(mCallback);
            mMetadata = mController.getMetadata();
            mState = mController.getPlaybackState();
            if (mTransportControl == null)
                mTransportControl = mController.getTransportControls();
            updateMetadata();
            updateControls();
        } else Log.e("ControlsFragment", "controller is null");
    }

    @Override
    public void onStop() {
        if (mController != null)
            mController.unregisterCallback(mCallback);
        super.onStop();
    }
}
