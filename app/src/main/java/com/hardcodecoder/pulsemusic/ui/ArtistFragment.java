package com.hardcodecoder.pulsemusic.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.audiofx.AudioEffect;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.DetailsActivity;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.activities.SearchActivity;
import com.hardcodecoder.pulsemusic.activities.SettingsActivity;
import com.hardcodecoder.pulsemusic.adapters.ArtistAdapter;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.ArtistModel;
import com.hardcodecoder.pulsemusic.loaders.ArtistFetcher;
import com.hardcodecoder.pulsemusic.utils.AppSettings;

import java.util.List;

public class ArtistFragment extends Fragment implements ItemClickListener.SingleEvent {

    private List<ArtistModel> mList;
    private ArtistAdapter adapter;
    private int spanCount;
    private int currentConfig;
    private GridLayoutManager layoutManager;
    private enum ID {
        PORTRAIT,
        LANDSCAPE
    }

    public ArtistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        setHasOptionsMenu(true);
        currentConfig = getResources().getConfiguration().orientation;
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar t = view.findViewById(R.id.toolbar);
        t.setTitle(R.string.artist);

        if (getActivity() != null)
            ((MainActivity) getActivity()).setSupportActionBar(t);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            spanCount = AppSettings.getLandscapeGridSpanCount(getContext());
        else
            spanCount = AppSettings.getPortraitGridSpanCount(getContext());

        new Handler().postDelayed(() -> setRv(view), 310);
        startPostponedEnterTransition();
    }

    private void setRv(View view) {
        if (null != getActivity())
            new ArtistFetcher(getActivity().getContentResolver(), data -> {
                if (null != data && data.size() > 0) {
                    mList = data;
                    RecyclerView rv = view.findViewById(R.id.rv_artist_fragment);
                    rv.setVisibility(View.VISIBLE);
                    layoutManager = new GridLayoutManager(rv.getContext(), spanCount);
                    rv.setLayoutManager(layoutManager);
                    rv.setHasFixedSize(true);
                    adapter = new ArtistAdapter(mList, getLayoutInflater(), this);
                    rv.setAdapter(adapter);
                }
            }, ArtistFetcher.SORT.TITLE_ASC).execute();
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

            case R.id.two:
                updateGridSize(ID.PORTRAIT, 2);
                break;
            case R.id.three:
                updateGridSize(ID.PORTRAIT, 3);
                break;
            case R.id.four:
                updateGridSize(ID.PORTRAIT, 4);
                break;

            case R.id.l_four:
                updateGridSize(ID.LANDSCAPE, 4);
                break;
            case R.id.l_five:
                updateGridSize(ID.LANDSCAPE, 5);
                break;
            case R.id.l_six:
                updateGridSize(ID.LANDSCAPE, 6);
                break;
        }
        return true;
    }

    private void updateGridSize(ID id, int spanCount){
        if(id == ID.PORTRAIT) {
            AppSettings.savePortraitGridSpanCount(getContext(), spanCount);
            if(currentConfig == Configuration.ORIENTATION_PORTRAIT)
                layoutManager.setSpanCount(spanCount);
        }
        else {
            AppSettings.saveLandscapeGridSpanCount(getContext(), spanCount);
            if(currentConfig == Configuration.ORIENTATION_LANDSCAPE)
                layoutManager.setSpanCount(spanCount);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentConfig = Configuration.ORIENTATION_LANDSCAPE;
            spanCount = AppSettings.getLandscapeGridSpanCount(getContext());
            layoutManager.setSpanCount(spanCount);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            currentConfig = Configuration.ORIENTATION_PORTRAIT;
            spanCount = AppSettings.getPortraitGridSpanCount(getContext());
            layoutManager.setSpanCount(spanCount);
        }
    }

    @Override
    public void onClickItem(int pos) {
        Intent i = new Intent(getContext(), DetailsActivity.class);
        i.putExtra(DetailsActivity.KEY_ART_URL, String.valueOf(R.drawable.album_art_error));
        i.putExtra(DetailsActivity.KEY_TITLE, mList.get(pos).getArtistName());
        i.putExtra(DetailsActivity.KEY_ITEM_CATEGORY, DetailsActivity.CATEGORY_ARTIST);
        startActivity(i);
    }
}
