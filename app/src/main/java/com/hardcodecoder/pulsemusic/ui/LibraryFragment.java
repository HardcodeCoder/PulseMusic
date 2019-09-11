package com.hardcodecoder.pulsemusic.ui;

import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.activities.SearchActivity;
import com.hardcodecoder.pulsemusic.activities.SettingsActivity;
import com.hardcodecoder.pulsemusic.adapters.LibraryAdapter;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.util.List;

public class LibraryFragment extends Fragment implements LibraryItemClickListener {

    private TrackManager tm;
    private List<MusicModel> mList = null;
    private MediaController.TransportControls mTransportControl;

    public LibraryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tm = TrackManager.getInstance();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.library);

        if (null != getActivity())
            ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        new Handler().postDelayed(() -> setRv(view), 310);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_library_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_search:
                startActivity(new Intent(getContext(), SearchActivity.class));
                break;

            case R.id.menu_action_equalizer:
                final Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                if (null != getContext()) {
                    if (intent.resolveActivity(getContext().getPackageManager()) != null)
                        startActivityForResult(intent, 599);
                    else
                        Toast.makeText(getContext(), getString(R.string.equalizer_error), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_action_setting:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                break;
        }
        return true;
    }

    private void setRv(final View v) {
        mList = tm.getMainList();
        if (null != mList) {
            RecyclerView recyclerView = v.findViewById(R.id.rv_library_fragment);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), RecyclerView.VERTICAL, false));
            recyclerView.setHasFixedSize(true);
            LibraryAdapter adapter = new LibraryAdapter(mList, getLayoutInflater(), this);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(int pos) {
        tm.buildDataList(mList, pos);
        play();
    }

    @Override
    public void onOptionsClick(int pos) {
        showMenuItems(mList.get(pos));
    }

    private void play() {
        if (null == mTransportControl && null != getActivity()) {
            mTransportControl = getActivity().getMediaController().getTransportControls();
            mTransportControl.play();
        } else
            mTransportControl.play();
    }

    private void showMenuItems(MusicModel md) {
        View view = View.inflate(getContext(), R.layout.library_item_menu, null);
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
}
