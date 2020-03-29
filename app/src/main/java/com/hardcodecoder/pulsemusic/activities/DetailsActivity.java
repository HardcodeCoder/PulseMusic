package com.hardcodecoder.pulsemusic.activities;

import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.adapters.DetailsAdapter;
import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.loaders.ItemsLoader;
import com.hardcodecoder.pulsemusic.ui.CustomBottomSheet;

import java.util.List;

public class DetailsActivity extends MediaSessionActivity implements AsyncTaskCallback.Simple, LibraryItemClickListener {

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
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        tm = TrackManager.getInstance();
        String art = getIntent().getStringExtra(KEY_ART_URL);
        title = getIntent().getStringExtra(KEY_TITLE);
        mCategory = getIntent().getIntExtra(KEY_ITEM_CATEGORY, -1);

        ImageView iv = findViewById(R.id.details_activity_art);

        GlideApp.with(this)
                .load(art)
                .error(getDrawable(mCategory == CATEGORY_ALBUM ? R.drawable.ic_album_art : R.drawable.ic_artist_art))
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
                .transform(GlideConstantArtifacts.getDefaultRoundingRadius())
                .into(iv);

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
            temp.setText(getString(R.string.num_tracks).concat(" "+mList.size() + " ").concat(getString(R.string.tracks_num)));

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
            playMedia();
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

    @Override
    public void onMediaServiceConnected(MediaController controller) {
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromMediaSession();
    }
}
