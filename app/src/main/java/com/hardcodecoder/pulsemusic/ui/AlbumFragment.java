package com.hardcodecoder.pulsemusic.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.audiofx.AudioEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.DetailsActivity;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.activities.SearchActivity;
import com.hardcodecoder.pulsemusic.activities.SettingsActivity;
import com.hardcodecoder.pulsemusic.adapters.AlbumsAdapter;
import com.hardcodecoder.pulsemusic.interfaces.AlbumDataFetchCompletionCallback;
import com.hardcodecoder.pulsemusic.interfaces.TransitionClickListener;
import com.hardcodecoder.pulsemusic.model.AlbumModel;
import com.hardcodecoder.pulsemusic.utils.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment implements TransitionClickListener {

    private List<AlbumModel> mList;
    private AlbumsAdapter adapter;
    private int spanCount;
    private int currentConfig = Configuration.ORIENTATION_PORTRAIT;
    private GridLayoutManager layoutManager;

    public AlbumFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar t = view.findViewById(R.id.toolbar);
        t.setTitle(R.string.albums);
        if (getActivity() != null)
            ((MainActivity) getActivity()).setSupportActionBar(t);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            spanCount = UserInfo.getLandscapeGridSpanCount(getContext());
        else spanCount = UserInfo.getPortraitGridSpanCount(getContext());
        setRv(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_album_artist_fragment, menu);
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
                    if ((intent.resolveActivity(getContext().getPackageManager()) != null))
                        startActivityForResult(intent, 599);
                    else
                        Toast.makeText(getContext(), getString(R.string.equalizer_error), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_action_setting:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentConfig = Configuration.ORIENTATION_LANDSCAPE;
            spanCount = UserInfo.getLandscapeGridSpanCount(getContext());
            layoutManager.setSpanCount(spanCount);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            currentConfig = Configuration.ORIENTATION_PORTRAIT;
            spanCount = UserInfo.getPortraitGridSpanCount(getContext());
            layoutManager.setSpanCount(spanCount);
        }
    }

    private void setRv(View view) {
        final Handler h = new Handler();
        if (null != getActivity()) new AlbumFetcher(getActivity().getContentResolver(), list -> {
            if (null != list && list.size() > 0) {
                mList = list;
                h.post(() -> {
                    RecyclerView rv = view.findViewById(R.id.rv_album_fragment);
                    layoutManager = new GridLayoutManager(rv.getContext(), spanCount);
                    rv.setLayoutManager(layoutManager);
                    rv.setHasFixedSize(true);
                    rv.setItemAnimator(new DefaultItemAnimator());
                    adapter = new AlbumsAdapter(list, getLayoutInflater(), this);
                    rv.setAdapter(adapter);
                });
            }
        }).execute();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (currentConfig == Configuration.ORIENTATION_PORTRAIT) {
            if (spanCount != UserInfo.getPortraitGridSpanCount(getContext()))
                layoutManager.setSpanCount(UserInfo.getPortraitGridSpanCount(getContext()));
        } else if (currentConfig == Configuration.ORIENTATION_LANDSCAPE) {
            if (spanCount != UserInfo.getLandscapeGridSpanCount(getContext()))
                layoutManager.setSpanCount(UserInfo.getLandscapeGridSpanCount(getContext()));
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        Intent i = new Intent(getContext(), DetailsActivity.class);
        i.putExtra(DetailsActivity.KEY_ART_URL, mList.get(pos).getAlbumArt());
        i.putExtra(DetailsActivity.KEY_TITLE, mList.get(pos).getAlbumName());
        i.putExtra(DetailsActivity.KEY_ITEM_CATEGORY, DetailsActivity.CATEGORY_ALBUM);
        startActivity(i);
    }

    static class AlbumFetcher extends AsyncTask<Void, Void, List<AlbumModel>> {

        private AlbumDataFetchCompletionCallback mCallback;
        private List<AlbumModel> data = new ArrayList<>();
        private ContentResolver mContentResolver;

        AlbumFetcher(ContentResolver mContentResolver, AlbumDataFetchCompletionCallback mCallback) {
            this.mCallback = mCallback;
            this.mContentResolver = mContentResolver;
        }

        @Override
        protected List<AlbumModel> doInBackground(Void... voids) {
            String[] col = {MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ALBUM_ART,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS};
            final Cursor cursor = mContentResolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    col,
                    null,
                    null,
                    MediaStore.Audio.Albums.ALBUM + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));

                    String album = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));

                    String albumArt = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));

                    int num = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS));

                    data.add(new AlbumModel(id, album, num, albumArt));
                } while (cursor.moveToNext());
                cursor.close();
            }
            return data;
        }

        @Override
        protected void onPostExecute(List<AlbumModel> albumModels) {
            mCallback.onTaskComplete(albumModels);
        }

    }
}
