package com.hardcodecoder.pulsemusic.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.AppInfo;
import com.hardcodecoder.pulsemusic.activities.DetailsActivity;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.activities.SearchActivity;
import com.hardcodecoder.pulsemusic.activities.SettingsActivity;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapter;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapter.LayoutStyle;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapterAlbum;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapterArtist;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.AlbumModel;
import com.hardcodecoder.pulsemusic.model.ArtistModel;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.loaders.AlbumFetcher;
import com.hardcodecoder.pulsemusic.loaders.ArtistFetcher;
import com.hardcodecoder.pulsemusic.loaders.ShuffledListProvider;
import com.hardcodecoder.pulsemusic.loaders.TrackFetcherFromStorage;
import com.hardcodecoder.pulsemusic.loaders.TrackFetcherFromStorage.Sort;
import com.hardcodecoder.pulsemusic.utils.UserInfo;
import com.hardcodecoder.pulsemusic.viewmodel.HomeContentVM;

import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PICK_AVATAR = 1500;
    private TrackManager tm;
    private MediaController.TransportControls mTransportControl;
    private final Handler mHandler = new Handler();
    private PopupMenu pm;
    private ImageView profilePic;

    private HomeContentVM mModel;
    private List<MusicModel> mYouMayLikeList;
    private List<MusicModel> mNewInLibraryList;
    private List<AlbumModel> mAlbumList;
    private List<ArtistModel> mArtistList;


    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        tm = TrackManager.getInstance();
        setHasOptionsMenu(true);
        mModel = HomeContentVM.getInstance();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.home);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        if (null != getActivity())
            ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> openDrawer());

        startPostponedEnterTransition();
        mHandler.postDelayed(() -> updateUi(view), 320);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search)
            startActivity(new Intent(getContext(), SearchActivity.class));
        return true;
    }

    private void updateUi(View view){
        mYouMayLikeList = mModel.getYourMayLikeList();
        mNewInLibraryList = mModel.getNewInLibrary();
        mAlbumList = mModel.getAlbumsList();
        mArtistList = mModel.getArtistList();

        if(null == mYouMayLikeList) {
            new ShuffledListProvider(list -> {
                mYouMayLikeList = list;
                mModel.setYourMayLikeList(mYouMayLikeList);
                loadRecycleView(view, R.id.home_suggested_rv, mYouMayLikeList, LayoutStyle.CIRCLE);
            }).execute();
        }
        else loadRecycleView(view, R.id.home_suggested_rv, mYouMayLikeList, LayoutStyle.CIRCLE);


        if(null == mNewInLibraryList && null != getContext()) {
            new TrackFetcherFromStorage(getContext().getContentResolver(), list -> {
                mNewInLibraryList = list;
                mModel.setNewInLibrary(mNewInLibraryList);
                loadRecycleView(view, R.id.new_in_library_rv, mNewInLibraryList, LayoutStyle.ROUNDED_RECTANGLE);
            }, Sort.DATE_ADDED_DESC).execute();
        }
        else loadRecycleView(view, R.id.new_in_library_rv, mNewInLibraryList, LayoutStyle.ROUNDED_RECTANGLE);


        if(null == mArtistList && null != getContext()){
            new ArtistFetcher(getContext().getContentResolver(), data -> {
                mArtistList = data.subList(0, ((int) (data.size()*0.10))); // sublist top 10%
                Collections.shuffle(mArtistList);
               mModel.setArtistList(mArtistList);
               loadArtistRv(view);
            }, ArtistFetcher.SORT.NUM_OF_TRACKS_DESC).execute();
        }
        else loadArtistRv(view);


        if(null == mAlbumList && null != getContext()) {
            new AlbumFetcher(getContext().getContentResolver(), data -> {
                mAlbumList = data.subList(0, ((int) (data.size()*0.10))); // sublist top 10%
                Collections.shuffle(mAlbumList);
                mModel.setAlbumsList(mAlbumList);
                loadAlbumCard(view);
            }, AlbumFetcher.SORT.TITLE_ASC).execute();
        }
        else loadAlbumCard(view);
    }

    private void loadRecycleView(View view, @IdRes int id, List<MusicModel> dataList, LayoutStyle style) {
        mHandler.post(() -> {
            RecyclerView rv = view.findViewById(id);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv.setHasFixedSize(true);
            HomeAdapter adapter = new HomeAdapter(getLayoutInflater(), dataList, new ItemClickListener.Simple() {
                @Override
                public void onItemClick(int pos) {
                    tm.buildDataList(dataList, pos);
                    play();
                }

                @Override
                public void onOptionsClick(View view, int pos) {
                    openMenu(dataList.get(pos), view);
                }
            }, style);
            rv.setAdapter(adapter);
        });
    }

    private void loadArtistRv(View v){
        mHandler.post(() -> {
           RecyclerView rv = v.findViewById(R.id.home_recent_artist_rv);
           rv.setVisibility(View.VISIBLE);
           rv.setHasFixedSize(true);
           rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), RecyclerView.HORIZONTAL, false));
           HomeAdapterArtist adapter = new HomeAdapterArtist(getLayoutInflater(), mArtistList, new ItemClickListener.Simple() {
               @Override
               public void onItemClick(int pos) {

               }

               @Override
               public void onOptionsClick(View view, int pos) {
                   Intent i = new Intent(getContext(), DetailsActivity.class);
                   i.putExtra(DetailsActivity.KEY_ART_URL, String.valueOf(R.drawable.album_art_error));
                   i.putExtra(DetailsActivity.KEY_TITLE, mArtistList.get(pos).getArtistName());
                   i.putExtra(DetailsActivity.KEY_ITEM_CATEGORY, DetailsActivity.CATEGORY_ARTIST);
                   Bundle b = null;
                   if(null != getActivity())
                       b = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, getString(R.string.home_iv_st)).toBundle();
                   startActivity(i, b);
               }
           });
           rv.setAdapter(adapter);
        });
    }

    private void loadAlbumCard(View v){
        mHandler.post(() -> {
            RecyclerView rv = v.findViewById(R.id.home_albums_rv);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv.setHasFixedSize(true);
            HomeAdapterAlbum adapter = new HomeAdapterAlbum(getLayoutInflater(), mAlbumList, new ItemClickListener.Simple() {
                @Override
                public void onItemClick(int pos) {
                }

                @Override
                public void onOptionsClick(View view, int pos) {
                    Intent i = new Intent(getContext(), DetailsActivity.class);
                    i.putExtra(DetailsActivity.KEY_ART_URL, mAlbumList.get(pos).getAlbumArt());
                    i.putExtra(DetailsActivity.KEY_TITLE, mAlbumList.get(pos).getAlbumName());
                    i.putExtra(DetailsActivity.KEY_ITEM_CATEGORY, DetailsActivity.CATEGORY_ALBUM);
                    Bundle b = null;
                    if(null != getActivity())
                    b = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, getString(R.string.home_iv_st)).toBundle();
                    startActivity(i, b);
                }
            });
            rv.setAdapter(adapter);
        });

    }

    private void openMenu(MusicModel md, View v) {
        v.setBackground(v.getContext().getDrawable(R.drawable.active_item_background));
        pm = new PopupMenu(v.getContext(), v);
        pm.getMenuInflater().inflate(R.menu.item_overflow__menu, pm.getMenu());
        pm.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.id_play_next:
                    tm.playNext(md);
                    Toast.makeText(v.getContext(), getString(R.string.play_next_toast), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.id_add_queue:
                    tm.addToActiveQueue(md);
                    Toast.makeText(v.getContext(), getString(R.string.add_to_queue_toast), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.id_add_playlist:
                    break;
                default:
            }
            return true;
        });
        pm.setOnDismissListener(menu -> v.setBackground(v.getResources().getDrawable(android.R.color.transparent)));
        pm.show();
    }

    private void openDrawer() {
        View v = View.inflate(getContext(), R.layout.home_bottom_sheet_dialog, null);
        BottomSheetDialog bottomSheetDialog = new CustomBottomSheet(v.getContext());
        bottomSheetDialog.setContentView(v);

        TextView tv = v.findViewById(R.id.home_greeting);
        if(null != getContext())
            tv.setText(UserInfo.getUserName(getContext()));
        tv.findViewById(R.id.home_greeting).setOnClickListener(v1 -> {
            addUserName();
            if(bottomSheetDialog.isShowing()) bottomSheetDialog.dismiss();
        });

        profilePic = v.findViewById(R.id.user_profile);
        profilePic.setOnClickListener(v1 -> pickPhoto());
        loadProfilePic();

        v.findViewById(R.id.check_source_code).
                setOnClickListener(v1 -> openLink(getString(R.string.source_code_link)));

        v.findViewById(R.id.git_profile).
                setOnClickListener(v1 -> openLink(getString(R.string.github_link)));

        v.findViewById(R.id.app_info).
                setOnClickListener(v1 -> startActivity(new Intent(v.getContext(), AppInfo.class)));

        v.findViewById(R.id.settings)
                .setOnClickListener(v1 -> {
                    startActivity(new Intent(v.getContext(), SettingsActivity.class));
                    if (bottomSheetDialog.isShowing()) bottomSheetDialog.dismiss();
                });
        bottomSheetDialog.show();
    }

    private void loadProfilePic() {
        GlideApp.with(this)
                .load(UserInfo.getUserProfilePic(profilePic.getContext()))
                .error(R.drawable.def_avatar)
                .circleCrop()
                .into(profilePic);
    }

    private void pickPhoto() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_AVATAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_AVATAR && resultCode == Activity.RESULT_OK) {
            if (null == data) {
                Toast.makeText(getContext(), "Cannot retrieve image", Toast.LENGTH_SHORT).show();
                return;
            }
            if(null != getContext()) {
                UserInfo.saveUserProfilePic(getContext(), data.getDataString());
                loadProfilePic();
            }
        }
    }

    private void openLink(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void addUserName() {
        View layout = View.inflate(getContext(), R.layout.dialog_create_playlist, null);
        BottomSheetDialog sheetDialog = new CustomBottomSheet(layout.getContext());
        sheetDialog.setContentView(layout);
        sheetDialog.show();

        ((TextView) layout.findViewById(R.id.header)).setText(getResources().getString(R.string.enter_name));
        ((TextInputLayout) layout.findViewById(R.id.edit_text_container)).setHint(getResources().getString(R.string.enter_name));
        TextInputEditText et = layout.findViewById(R.id.text_input_field);

        layout.findViewById(R.id.confirm_btn)
                .setOnClickListener(v -> {
                    if (null != getContext() && et.getText() != null && et.getText().toString().length() > 0) {
                        UserInfo.saveUserName(getContext(), et.getText().toString());
                        if (sheetDialog.isShowing()) sheetDialog.dismiss();
                    } else
                        Toast.makeText(v.getContext(), getString(R.string.enter_name_toast), Toast.LENGTH_SHORT).show();
                });

        layout.findViewById(R.id.cancel_btn)
                .setOnClickListener(v -> {
                    if (sheetDialog.isShowing())
                        sheetDialog.dismiss();
                });

    }

    private void play() {
        if (mTransportControl != null) {
            mTransportControl.play();
        } else if (getActivity() != null) {
            mTransportControl = getActivity().getMediaController().getTransportControls();
            play();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != pm)
            pm.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*TrackCache.getInstance().holdPickedTracks(shuffledTracks);
        TrackCache.getInstance().cacheRecentlyAdded(recentlyAdded);
        savedTracks.clear();*/
    }
}