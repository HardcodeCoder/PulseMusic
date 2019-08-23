package com.hardcodecoder.pulsemusic.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.TrackPickerAdapter;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackCache;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.utils.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class TrackPickerActivity extends Activity implements ItemClickListener.Selector {

    private List<MusicModel> pickedTracks = new ArrayList<>();
    private List<MusicModel> masterList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(UserInfo.getThemeToApply());
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_picker);

        findViewById(R.id.btn_done).setOnClickListener(v -> {
            dispatchUpdatedTrack();
            finish();
            overrideActivityTransition();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.select_tracks));
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overrideActivityTransition();
        });

        masterList = TrackManager.getInstance().getMainList();
        RecyclerView recyclerView = findViewById(R.id.track_picker_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        TrackPickerAdapter adapter = new TrackPickerAdapter(masterList, getLayoutInflater(), this);

        recyclerView.setAdapter(adapter);
    }

    private void overrideActivityTransition() {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overrideActivityTransition();
    }

    @Override
    public void onSelected(MusicModel md) {
        pickedTracks.add(md);
    }

    @Override
    public void onUnselected(MusicModel md) {
        pickedTracks.remove(md);
    }

    private void dispatchUpdatedTrack() {
        if (pickedTracks.size() > 0)
            TrackCache.getInstance().holdPickedTracks(pickedTracks);
        masterList = null;
    }

}
