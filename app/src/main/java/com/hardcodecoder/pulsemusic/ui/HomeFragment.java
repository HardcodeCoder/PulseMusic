package com.hardcodecoder.pulsemusic.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.AppInfo;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.activities.SearchActivity;
import com.hardcodecoder.pulsemusic.activities.SettingsActivity;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapter;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapter.LayoutStyle;
import com.hardcodecoder.pulsemusic.adapters.HomeAdapterArtist;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackCache;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.tasks.ShuffledListProvider;
import com.hardcodecoder.pulsemusic.tasks.TrackFetcherFromStorage;
import com.hardcodecoder.pulsemusic.tasks.TrackFetcherFromStorage.Sort;
import com.hardcodecoder.pulsemusic.utils.PlaylistStorageManager;
import com.hardcodecoder.pulsemusic.utils.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PICK_AVATAR = 1500;
    private TrackManager tm;
    private MediaController.TransportControls mTransportControl;
    private List<MusicModel> shuffledTracks = null;
    private List<MusicModel> savedTracks = new ArrayList<>();
    private List<MusicModel> recentlyAdded = null;
    private List<MusicModel> recentArtist = new ArrayList<>();
    private final Handler mHandler = new Handler();
    private PopupMenu pm;
    private ImageView profilePic;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tm = TrackManager.getInstance();
        setHasOptionsMenu(true);
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

        mHandler.postDelayed(() -> loadContents(view), 310);
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

    private void loadContents(View view) {
        shuffledTracks = TrackCache.getInstance().giveTracks();
        //If there's no records found create new shuffled tracks
        if (shuffledTracks == null) new ShuffledListProvider(list -> {
            shuffledTracks = list;
            loadRecycleView(view, R.id.home_suggested_rv, shuffledTracks, LayoutStyle.ROUNDED_RECTANGLE);
        }).execute();
        else
            loadRecycleView(view, R.id.home_suggested_rv, shuffledTracks, LayoutStyle.ROUNDED_RECTANGLE);

        //Get saved history
        List<MusicModel> temp = PlaylistStorageManager.getRecentTracks(getContext());
        if (null != temp) savedTracks.addAll(temp);
        //Also add current played tracks from track manager
        savedTracks.addAll(tm.getCurrentPlayedTracks());
        //Load recycler view if the list is not empty
        if (savedTracks.size() > 0) loadRecentRv(view);
        else view.findViewById(R.id.home_heading_recent).setVisibility(View.GONE);

        //Check for cached data
        recentlyAdded = TrackCache.getInstance().recentlyAddedTracks();
        //If no cached data found get it from database
        if (null == recentlyAdded && null != getContext())
            new TrackFetcherFromStorage(getContext().getContentResolver(), list -> mHandler.post(() -> {
                recentlyAdded = list;
                loadRecycleView(view, R.id.new_in_library_rv, recentlyAdded, LayoutStyle.CIRCLE);
                //Load recent artist after recently added as it's data depends upon recently added data
                loadRecentArtist(view);
            }), Sort.DATE_ADDED_DESC).execute();
        else {
            loadRecycleView(view, R.id.new_in_library_rv, recentlyAdded, LayoutStyle.CIRCLE);
            //Load recent artist after recently added as it's data depends upon recently added data
            loadRecentArtist(view);
        }
    }

    private void loadRecentArtist(View view) {
        //Load recent artist from recent tracks as well as new in library section (50%-50%)
        //If recent section is empty get all from new in library section
        //Sort Priority -> New in Library
        int size = savedTracks.size();
        if (size > 0) {
            int endIndex = 5;
            if (size < 5) endIndex = size;
            recentArtist.addAll(savedTracks.subList(0, endIndex));
            recentArtist.addAll(recentlyAdded.subList(0, 5));
        } else recentArtist.addAll(recentlyAdded.subList(0, 10));
        mHandler.post(() -> {
            RecyclerView rv = view.findViewById(R.id.home_recent_artist_rv);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setHasFixedSize(true);
            HomeAdapterArtist adapter = new HomeAdapterArtist(getLayoutInflater(), recentArtist, new ItemClickListener.Simple() {
                @Override
                public void onItemClick(int pos) {
                    tm.buildDataList(recentArtist, pos);
                    play();
                }

                @Override
                public void onOptionsClick(View view, int pos) {
                    openMenu(recentArtist.get(pos), view);
                }
            });
            rv.setAdapter(adapter);
        });
    }

    private void loadRecycleView(View view, @IdRes int id, List<MusicModel> dataList, LayoutStyle style) {
        mHandler.post(() -> {
            RecyclerView rv = view.findViewById(id);
            rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv.setItemAnimator(new DefaultItemAnimator());
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

    private void loadRecentRv(View v) {
        mHandler.post(() -> {
            RecyclerView recyclerView = v.findViewById(R.id.home_recent_rv);
            LinearLayoutManager llm = new LinearLayoutManager(recyclerView.getContext(), RecyclerView.HORIZONTAL, true);
            llm.setStackFromEnd(true);
            recyclerView.setLayoutManager(llm);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            HomeAdapter adapter = new HomeAdapter(getLayoutInflater(), savedTracks, new ItemClickListener.Simple() {
                @Override
                public void onItemClick(int pos) {
                    tm.buildDataList(savedTracks, pos);
                    play();
                }

                @Override
                public void onOptionsClick(View view1, int pos) {
                    openMenu(savedTracks.get(pos), view1);
                }
            });
            recyclerView.setAdapter(adapter);
        });
    }

    private void openMenu(MusicModel md, View v) {
        Context wrapper = new ContextThemeWrapper(v.getContext(), R.style.PopupMenu);
        v.setBackground(v.getResources().getDrawable(R.drawable.active_item_background));
        pm = new PopupMenu(wrapper, v);
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
        tv.setText(UserInfo.getUserName());
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
                .load(UserInfo.getUserProfilePic())
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
            UserInfo.saveUserProfilePic(data.getDataString());
            loadProfilePic();
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
                    if (et.getText() != null && et.getText().toString().length() > 0) {
                        UserInfo.saveUserName(et.getText().toString());
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
        TrackCache.getInstance().holdPickedTracks(shuffledTracks);
        TrackCache.getInstance().cacheRecentlyAdded(recentlyAdded);
        savedTracks.clear();
    }
}