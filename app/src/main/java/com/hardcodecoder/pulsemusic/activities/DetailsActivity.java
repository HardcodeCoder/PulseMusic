package com.hardcodecoder.pulsemusic.activities;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.PMS;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.DetailsAdapter;
import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.loaders.ItemsLoader;
import com.hardcodecoder.pulsemusic.themes.ThemeManager;
import com.hardcodecoder.pulsemusic.ui.CustomBottomSheet;

import java.util.List;

public class DetailsActivity extends AppCompatActivity implements AsyncTaskCallback.Simple, LibraryItemClickListener {

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
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        connectToSession();
        setContentView(R.layout.activity_details);
        tm = TrackManager.getInstance();
        String art = getIntent().getStringExtra(KEY_ART_URL);
        title = getIntent().getStringExtra(KEY_TITLE);
        mCategory = getIntent().getIntExtra(KEY_ITEM_CATEGORY, -1);

        ImageView iv = findViewById(R.id.details_activity_art);

        if (mCategory == CATEGORY_ALBUM) {
            GlideApp.with(this)
                    .load(art)
                    .error(getDrawable(R.drawable.album_art_error))
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(iv);
        }
        else if (mCategory == CATEGORY_ARTIST) {
            iv.setImageResource(R.drawable.artist_art_error);
            supportStartPostponedEnterTransition();
        }

        findViewById(R.id.details_activity_btn_close).setOnClickListener(v -> finishAfterTransition());
        loadItems();
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
            rv.setVisibility(View.VISIBLE);
            rv.setHasFixedSize(true);
            LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(rv.getContext(), R.anim.item_slide_up_animation);
            rv.setLayoutAnimation(controller);
            rv.setVerticalFadingEdgeEnabled(true);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), RecyclerView.VERTICAL, false));
            DetailsAdapter adapter = new DetailsAdapter(list, this, getLayoutInflater());
            rv.setAdapter(adapter);
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
                    if(bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
                });

        view.findViewById(R.id.add_to_queue)
                .setOnClickListener(v -> {
                    tm.addToActiveQueue(mList.get(pos));
                    Toast.makeText(v.getContext(), getString(R.string.add_to_queue_toast), Toast.LENGTH_SHORT).show();
                    if(bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
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
    public void onBackPressed() {
        finishAfterTransition();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaBrowser != null)
            mMediaBrowser.disconnect();
    }
}
