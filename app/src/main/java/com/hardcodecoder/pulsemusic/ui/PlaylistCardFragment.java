package com.hardcodecoder.pulsemusic.ui;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.MainActivity;
import com.hardcodecoder.pulsemusic.activities.PlaylistDataActivity;
import com.hardcodecoder.pulsemusic.activities.SearchActivity;
import com.hardcodecoder.pulsemusic.activities.SettingsActivity;
import com.hardcodecoder.pulsemusic.adapters.CardsAdapter;
import com.hardcodecoder.pulsemusic.helper.RecyclerViewGestureHelper;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.interfaces.RecyclerViewGestures;
import com.hardcodecoder.pulsemusic.utils.PlaylistStorageManager;

import java.util.List;

public class PlaylistCardFragment extends Fragment implements ItemClickListener.Cards, RecyclerViewGestures.GestureCallback {

    private Context mContext;
    private List<String> playlistNames = null;
    private boolean isPlaylistNamesModified = false;
    private CardsAdapter adapter;

    public PlaylistCardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (getContext() != null)
            mContext = getContext();
        return inflater.inflate(R.layout.fragment_playlist_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton fab = view.findViewById(R.id.btn_add_playlist);
        fab.setOnClickListener(v -> openBottomDialog(false, -1));

        playlistNames = PlaylistStorageManager.getPlaylistNames(mContext);
        addPendingPlaylistTiles(view);

        Toolbar t = view.findViewById(R.id.toolbar);
        t.setTitle(R.string.playlist_nav);
        if (null != getActivity())
            ((MainActivity) getActivity()).setSupportActionBar(t);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playlist_card_fragment, menu);
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
        return true;
    }

    private void addPendingPlaylistTiles(@NonNull final View view) {
        RecyclerView recyclerView = view.findViewById(R.id.playlist_cards_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(), R.anim.item_falls_down_animation);
        recyclerView.setLayoutAnimation(controller);
        adapter = new CardsAdapter(playlistNames, this, getLayoutInflater());
        recyclerView.setAdapter(adapter);

        /*
         * Adding swipe gesture to delete playlist card
         */
        ItemTouchHelper.Callback itemTouchHelperCallback = new RecyclerViewGestureHelper(this/*, mActivity*/);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onItemSwiped(int itemAdapterPosition) {
        //First two item in the list are the playing queue and favourites,
        //which should not be user deletable
        if(itemAdapterPosition <= 1){
            Toast.makeText(mContext, "Cannot delete delete playlist card", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(itemAdapterPosition);
            return;
        }
        playlistNames.remove(itemAdapterPosition);

        //Explicitly deleting list of tracks playlist card holds
        //because playlist title info gets updated at #onStop method
        //but playlist card data is not
        PlaylistStorageManager.dropPlaylistCardDataAt(mContext, itemAdapterPosition);

        Toast.makeText(getContext(), "Playlist deleted", Toast.LENGTH_SHORT).show();
        //adapter.notifyItemRemoved(itemAdapterPosition);

        //This will make sure that the playlist title is deleted
        //It will trigger the commands in #onStop
        isPlaylistNamesModified = true;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        //Do nothing
    }

    @Override
    public void onItemMoved(int fromPos, int toPos) {
        //Do nothing
    }

    @Override
    public void onClearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        //Background was not changed during swipe gesture , so nothing to restore
    }

    private void addPlaylist(String s) {
        playlistNames.add(s);
        adapter.notifyItemInserted(playlistNames.size() - 1);
        isPlaylistNamesModified = true;
    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(mContext, PlaylistDataActivity.class);
        i.putExtra(PlaylistDataActivity.ITEM_NUMBER_KEY, position);
        i.putExtra(PlaylistDataActivity.TITLE_KEY, playlistNames.get(position));
        startActivity(i);
    }

    @Override
    public void onEdit(int pos) {
        openBottomDialog(true, pos);
    }

    /**
     * @param isEdit indicates whether the bottom sheet dialog is for editing playlist name
     * @param pos    Position at which the playlist name is edited ,only required if isEdit is true
     */
    private void openBottomDialog(boolean isEdit, int pos) {
        if (getContext() != null) {

            // Inflating the layout
            View layout = View.inflate(mContext, R.layout.dialog_create_playlist, null);

            //Setting up bottom sheet dialog
            BottomSheetDialog sheetDialog = new CustomBottomSheet(mContext);
            sheetDialog.setContentView(layout);
            sheetDialog.show();

            TextView header = layout.findViewById(R.id.header);
            header.setText(getResources().getString(isEdit ? R.string.edit_playlist : R.string.create_playlist));

            TextInputLayout til = layout.findViewById(R.id.edit_text_container);
            til.setHint(getResources().getString(R.string.create_playlist_hint));

            TextInputEditText et = layout.findViewById(R.id.text_input_field);

            Button create = layout.findViewById(R.id.confirm_btn);
            create.setOnClickListener(v ->
            {
                if (et.getText() != null && et.getText().toString().length() > 0) {
                    if (isEdit) {
                        playlistNames.remove(pos);
                        playlistNames.add(pos, et.getText().toString());
                        isPlaylistNamesModified = true;
                        adapter.notifyItemChanged(pos);
                    } else addPlaylist(et.getText().toString());
                } else {
                    Toast.makeText(mContext, "Please enter playlist's name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sheetDialog.isShowing())
                    sheetDialog.dismiss();
            });

            Button cancel = layout.findViewById(R.id.cancel_btn);
            cancel.setOnClickListener(v -> {
                if (sheetDialog.isShowing())
                    sheetDialog.dismiss();
            });
        }
    }

    @Override
    public void onStop() {
        if (playlistNames.size() > 0 && isPlaylistNamesModified) {
            PlaylistStorageManager.savePlaylistTitles(mContext, playlistNames);
            isPlaylistNamesModified = false;
        }

        /*
         * If all the playlist cards are removed then drop all playlist info
         * see @PlaylistStorageManager.dropAllPlaylistData for info
         */
        else if (isPlaylistNamesModified) PlaylistStorageManager.dropAllPlaylistData(mContext);

        super.onStop();
    }
}
