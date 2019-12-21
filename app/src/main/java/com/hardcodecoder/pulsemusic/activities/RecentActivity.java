package com.hardcodecoder.pulsemusic.activities;

import android.media.session.MediaController;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.LibraryAdapter;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.ui.CustomBottomSheet;
import com.hardcodecoder.pulsemusic.utils.PlaylistStorageManager;

import java.util.List;

public class RecentActivity extends MediaSessionActivity implements LibraryItemClickListener {

    private List<MusicModel> data;
    private TrackManager tm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent);

        tm = TrackManager.getInstance();
        data = PlaylistStorageManager.getRecentTracks(this);
        if(null != data && data.size() > 0) {
            RecyclerView rv = findViewById(R.id.rv_recently_played);
            rv.setVisibility(View.VISIBLE);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), RecyclerView.VERTICAL, false));
            LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(rv.getContext(), R.anim.item_slide_up_animation);
            rv.setLayoutAnimation(controller);
            LibraryAdapter adapter = new LibraryAdapter(data, getLayoutInflater(), this);
            rv.setAdapter(adapter);
        }
        else findViewById(R.id.no_tracks_fount_tv).setVisibility(View.VISIBLE);

        Toolbar t = findViewById(R.id.toolbar);
        t.setTitle(R.string.recent);
        t.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onItemClick(int pos) {
        tm.buildDataList(data, pos);
        playMedia();
    }

    @Override
    public void onOptionsClick(int pos) {
        showMenuItems(data.get(pos));
    }

    private void showMenuItems(MusicModel md) {
        View view = View.inflate(this, R.layout.library_item_menu, null);
        BottomSheetDialog bottomSheetDialog = new CustomBottomSheet(view.getContext());

        view.findViewById(R.id.track_play_next)
                .setOnClickListener(v -> {
                    tm.playNext(md);
                    Toast.makeText(v.getContext(), getString(R.string.play_next_toast), Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
                });

        view.findViewById(R.id.add_to_queue)
                .setOnClickListener(v -> {
                    tm.addToActiveQueue(md);
                    Toast.makeText(v.getContext(), getString(R.string.add_to_queue_toast), Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
                });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

   @Override
    public void onMediaServiceConnected(MediaController controller) {
    }
}
