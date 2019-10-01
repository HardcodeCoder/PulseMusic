package com.hardcodecoder.pulsemusic.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.PMS;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.SearchAdapter;
import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity implements ItemClickListener.Simple, AsyncTaskCallback.Simple {

    private List<MusicModel> mSearchResult;
    private List<String> pendingUpdates = new ArrayList<>();
    private MediaBrowser mMediaBrowser;
    private MediaController mController;
    private SearchAdapter adapter;
    private TextView tv;
    private TrackManager tm;
    private final Handler mHandler = new Handler();
    private String mQuery = "";
    private RecyclerView rv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeManager.getThemeToApply());
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        connectToSession();

        findViewById(R.id.search_activity_close_btn).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        });

        tv = findViewById(R.id.result_empty);
        setUpSearchView();
        setRecyclerView();
        tm = TrackManager.getInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    private void setUpSearchView() {
        SearchView sv = findViewById(R.id.search_view);
        View v = sv.findViewById(R.id.search_plate);
        v.setBackgroundColor(Color.parseColor("#00000000"));
        sv.setIconified(false);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(mQuery))
                    searchSuggestions(query);
                sv.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSuggestions(newText);
                return true;
            }
        });
    }

    private void searchSuggestions(String query) {
        mQuery = query;
        pendingUpdates.add(mQuery);
        if (pendingUpdates.size() == 1)
            mHandler.post(() -> new AsyncSearchTask(query, this).execute());
    }

    @Override
    public void onTaskComplete(List<MusicModel> list) {
        pendingUpdates.remove(0);
        this.mSearchResult = list;

        if (mSearchResult.size() <= 0) tv.setVisibility(View.VISIBLE);
        else tv.setVisibility(View.GONE);

        adapter.updateItems(list);

        if (pendingUpdates.size() > 0) {
            searchSuggestions(pendingUpdates.get(pendingUpdates.size() - 1));
            pendingUpdates.clear();
        }
    }

    private void setRecyclerView() {
        rv = findViewById(R.id.search_rv);
        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv.setItemAnimator(new DefaultItemAnimator());
        adapter = new SearchAdapter(getLayoutInflater(), this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        tm.buildDataList(mSearchResult, pos);
        mController.getTransportControls().play();
    }

    @Override
    public void onOptionsClick(View v, int position) {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
        PopupMenu pm = new PopupMenu(wrapper, v);
        pm.getMenuInflater().inflate(R.menu.item_overflow__menu, pm.getMenu());
        pm.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.id_play_next:
                    tm.playNext(mSearchResult.get(position));
                    break;
                case R.id.id_add_queue:
                    tm.addToActiveQueue(mSearchResult.get(position));
                    break;
                case R.id.id_add_playlist:
                    break;
                case R.id.info:
                    createDialog(position);
                    break;
                default:
            }
            return true;
        });
        pm.show();
    }

    private void createDialog(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        MusicModel md = mSearchResult.get(pos);
        builder.setTitle(md.getSongName());
        String s = getString(R.string.album_head) + " " + md.getAlbum() + "\n" + getString(R.string.artist_head) + " " + md.getArtist();
        builder.setMessage(s);
        builder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

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
                            mController = new MediaController(SearchActivity.this, token);
                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            setMediaController(mController);
                        } catch (Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                },
                null); // optional Bundle
        mMediaBrowser.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pendingUpdates.clear();
        if (mMediaBrowser != null)
            mMediaBrowser.disconnect();
        rv.setAdapter(null);

    }

    static class AsyncSearchTask extends AsyncTask<Void, Void, List<MusicModel>> {

        private List<MusicModel> listToReturn = new ArrayList<>();
        private AsyncTaskCallback.Simple mCallback;
        private String mText;

        AsyncSearchTask(String q, AsyncTaskCallback.Simple callback) {
            mText = q;
            this.mCallback = callback;
        }

        private List<MusicModel> filter() {
            List<MusicModel> listToWorkOn = TrackManager.getInstance().getMainList();
            listToReturn.clear();
            if (!mText.isEmpty() && null != listToWorkOn) {
                mText = mText.toLowerCase();
                for (MusicModel musicModel : listToWorkOn) {
                    if (musicModel.getSongName().toLowerCase().contains(mText) ||
                            musicModel.getAlbum().toLowerCase().contains(mText) ||
                            musicModel.getArtist().toLowerCase().contains(mText)) {
                        listToReturn.add(musicModel);
                    }
                }
            }
            return listToReturn;
        }

        @Override
        protected List<MusicModel> doInBackground(Void... voids) {
            return filter();
        }

        @Override
        protected void onPostExecute(List<MusicModel> musicModels) {
            mCallback.onTaskComplete(musicModels);
        }
    }
}

