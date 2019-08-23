package com.hardcodecoder.pulsemusic.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hardcodecoder.pulsemusic.PMS;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.PlaylistDataAdapter;
import com.hardcodecoder.pulsemusic.helper.RecyclerViewGestureHelper;
import com.hardcodecoder.pulsemusic.interfaces.ClickDragRvListener;
import com.hardcodecoder.pulsemusic.interfaces.RecyclerViewGestures;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackCache;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.utils.PlaylistStorageManager;
import com.hardcodecoder.pulsemusic.utils.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistDataActivity extends Activity implements ClickDragRvListener, RecyclerViewGestures.GestureCallback {

    public static final String TITLE_KEY = "playlist name";
    public static final String ITEM_NUMBER_KEY = "playlist number";
    private boolean isCurrentQueue = false;
    private List<MusicModel> mList = new ArrayList<>();
    private PlaylistDataAdapter adapter;
    private boolean isPlaylistDataModified = false;
    private int mPlaylistCardIndex;
    private MediaBrowser mMediaBrowser;
    private TrackManager tm;
    private ItemTouchHelper itemTouchHelper;
    private MediaController mController;
    private String playlistName;
    private boolean isFavPlaylist = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(UserInfo.getThemeToApply());
        super.onCreate(savedInstanceState);
        connectToSession();
        tm = TrackManager.getInstance();
        setContentView(R.layout.activity_playlist_data);
        if (getIntent().getExtras() != null) {
            playlistName = getIntent().getExtras().getString(TITLE_KEY);
            mPlaylistCardIndex = getIntent().getExtras().getInt(ITEM_NUMBER_KEY);
            if (mPlaylistCardIndex == 0) isCurrentQueue = true;
            else if (mPlaylistCardIndex == 1) isFavPlaylist = true;
            updateData();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(playlistName);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void updateData() {
        if (isCurrentQueue && null != tm.getActiveQueue())
            mList.addAll(tm.getActiveQueue());
        else if (isFavPlaylist)
            mList = PlaylistStorageManager.getFavorite(this);
        else if (!isCurrentQueue)
            mList = PlaylistStorageManager.getPlaylistTrackAtPosition(this, mPlaylistCardIndex - 2);

        FloatingActionButton fab = findViewById(R.id.open_track_picker_btn);
        fab.setOnClickListener(v -> startActivity(new Intent(this, TrackPickerActivity.class)));
        setRv();
    }

    private void setRv() {
        RecyclerView recyclerView = findViewById(R.id.playlist_data_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PlaylistDataAdapter(this, getLayoutInflater());
        recyclerView.setAdapter(adapter);

        /*
         * Setting up the swipe gestures
         */
        ItemTouchHelper.Callback itemTouchHelperCallback = new RecyclerViewGestureHelper(this/*, mActivity*/);
        itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        if (isCurrentQueue)
            recyclerView.scrollToPosition(TrackManager.getInstance().getActiveIndex());
    }

    @Override
    public void onItemClick(int position) {
        tm.buildDataList(mList, position);
        play();
    }

    @Override
    public void initiateDrag(RecyclerView.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
        holder.itemView.setBackground(getDrawable(R.drawable.active_item_background));
    }

    /**
     * New implementation
     *
     * @param itemAdapterPosition provides the index of the current item
     */
    @Override
    public void onItemSwiped(final int itemAdapterPosition) {
        if (isCurrentQueue) {
            if (itemAdapterPosition == tm.getActiveIndex()) {
                Toast.makeText(this, "Cannot remove active item", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(itemAdapterPosition);
                return;
            } else {
                if (!tm.canRemoveItem(itemAdapterPosition)) {
                    Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        MusicModel del = mList.remove(itemAdapterPosition);
        isPlaylistDataModified = true;
        adapter.removeItem(itemAdapterPosition);
        if (isCurrentQueue) tm.removeItemFromActiveQueue(itemAdapterPosition);

        Snackbar sb = Snackbar.make(findViewById(R.id.playlist_data_root_view), R.string.item_removed, Snackbar.LENGTH_SHORT);
        sb.setAction("UNDO", v -> {
            mList.add(itemAdapterPosition, del);
            adapter.restoreItem();
            if (isCurrentQueue)
                tm.restoreItem();
        });
        sb.show();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mList, fromPosition, toPosition);
        adapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemMoved(int fromPos, int toPos) {
        if (isCurrentQueue)
            tm.updateActiveQueue(fromPos, toPos);
        isPlaylistDataModified = true;
    }

    @Override
    public void onClearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackground(recyclerView.getResources().getDrawable(android.R.color.transparent));
    }

    private void play() {
        if (mController != null)
            mController.getTransportControls().play();
    }

    private void connectToSession() {
        mMediaBrowser = new MediaBrowser(this,
                new ComponentName(this, PMS.class),
                // Which MediaBrowserService
                new MediaBrowser.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            // Ah, here’s our Token again
                            MediaSession.Token token = mMediaBrowser.getSessionToken();
                            // This is what gives us access to everything
                            mController = new MediaController(PlaylistDataActivity.this, token);
                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            setMediaController(mController);
                            //mController.registerCallback(mCallback);
                        } catch (Exception e) {
                            Log.e(PlaylistDataActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                },
                null); // optional Bundle
        mMediaBrowser.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<MusicModel> tracksToAdd = TrackCache.getInstance().giveTracks();
        if (null != tracksToAdd && tracksToAdd.size() > 0) {
            mList.addAll(tracksToAdd);
            isPlaylistDataModified = true;
            if (isCurrentQueue) tm.buildDataList(mList, tm.getActiveIndex());
        }
        if (mList.size() > 0 && null != adapter) adapter.addItems(mList);
    }

    @Override
    protected void onStop() {
        if (isPlaylistDataModified && isFavPlaylist) {
            PlaylistStorageManager.saveFavorite(this, mList);
            isPlaylistDataModified = false;
        }
        if (isPlaylistDataModified && !isCurrentQueue) {
            PlaylistStorageManager.updatePlaylistTracks(this, mList, mPlaylistCardIndex - 2);
            isPlaylistDataModified = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
    }
}
