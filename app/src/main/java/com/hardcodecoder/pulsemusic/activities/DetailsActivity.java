package com.hardcodecoder.pulsemusic.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.PMS;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.DetailsAdapter;
import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.tasks.ItemsLoader;
import com.hardcodecoder.pulsemusic.themes.ThemeManager;
import com.hardcodecoder.pulsemusic.ui.CustomBottomSheet;

import java.util.List;

public class DetailsActivity extends Activity implements AsyncTaskCallback.Simple, LibraryItemClickListener {

    private MediaBrowser mMediaBrowser;
    private String title;
    private List<MusicModel> mList;
    private TrackManager tm;
    public static final String KEY_ART_URL = "art";
    public static final String KEY_TITLE = "title";
    public static final String KEY_ITEM_CATEGORY = "category";
    public static final int CATEGORY_ALBUM = 1;
    public static final int CATEGORY_ARTIST = 2;
    private int mCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeManager.getThemeToApply());

        super.onCreate(savedInstanceState);
        connectToSession();
        setContentView(R.layout.activity_details);
        String art = getIntent().getStringExtra(KEY_ART_URL);
        title = getIntent().getStringExtra(KEY_TITLE);
        mCategory = getIntent().getIntExtra(KEY_ITEM_CATEGORY, -1);
        if (mCategory == CATEGORY_ALBUM) {
            GlideApp.with(this)
                    .load(art)
                    .transform(GlideConstantArtifacts.getDefaultRoundingRadius())
                    .error(getDrawable(R.drawable.album_art_error))
                    .into((ImageView) findViewById(R.id.details_activity_art));
        }
        else if (mCategory == CATEGORY_ARTIST) {
            ImageView iv = findViewById(R.id.details_activity_art);
            iv.setImageResource(R.drawable.artist_art_error);
        }

        findViewById(R.id.details_activity_btn_close).setOnClickListener(v -> finish());

        loadItems();
        tm = TrackManager.getInstance();
    }

    private void loadItems() {
        new ItemsLoader(this, title, mCategory).execute();
    }

    @Override
    public void onTaskComplete(List<MusicModel> list) {
        if (list.size() > 0) {
            mList = list;

            TextView temp = findViewById(R.id.details_activity_title);
            temp.setText(title);
            temp = findViewById(R.id.details_activity_title_sub);
            temp.setText(getString(R.string.num_tracks).concat(String.valueOf(mList.size())));

            RecyclerView rv = findViewById(R.id.details_activity_rv);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), RecyclerView.VERTICAL, false));

            DetailsAdapter adapter = new DetailsAdapter(list, this, getLayoutInflater());
            rv.setItemAnimator(new DefaultItemAnimator());

            rv.setAdapter(adapter);
            rv.setVerticalFadingEdgeEnabled(true);
        }
    }

    @Override
    public void onItemClick(int pos) {
        if (null != mList) {
            tm.buildDataList(mList, pos);
            getMediaController().getTransportControls().play();
        }
    }

    @Override
    public void onOptionsClick(int pos) {
        View view = View.inflate(this, R.layout.library_item_menu, null);
        BottomSheetDialog bottomSheetDialog = new CustomBottomSheet(view.getContext());

        view.findViewById(R.id.track_play_next)
                .setOnClickListener(v -> {
                    tm.playNext(mList.get(pos));
                    Toast.makeText(v.getContext(), getString(R.string.play_next_toast), Toast.LENGTH_SHORT).show();
                });

        view.findViewById(R.id.add_to_queue)
                .setOnClickListener(v -> {
                    tm.addToActiveQueue(mList.get(pos));
                    Toast.makeText(v.getContext(), getString(R.string.add_to_queue_toast), Toast.LENGTH_SHORT).show();
                });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void connectToSession() {
        mMediaBrowser = new MediaBrowser(this, new ComponentName(DetailsActivity.this, PMS.class),
                // Which MediaBrowserService
                new MediaBrowser.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            // Ah, here’s our Token again
                            MediaSession.Token token = mMediaBrowser.getSessionToken();
                            // This is what gives us access to everything
                            MediaController mController = new MediaController(DetailsActivity.this, token);
                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            setMediaController(mController);
                            //mController.registerCallback(mCallback);
                        } catch (Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                }, null); // optional Bundle
        mMediaBrowser.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaBrowser != null)
            mMediaBrowser.disconnect();
    }
}
