package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.MyLibraryViewHolder> {

    private List<MusicModel> mList;
    private LibraryItemClickListener mListener;
    private LayoutInflater mInflater;
    private int lastPosition = -1;

    public LibraryAdapter(List<MusicModel> list, LayoutInflater inflater, LibraryItemClickListener listener) {
        this.mList = list;
        this.mListener = listener;
        this.mInflater = inflater;
    }

    @NonNull
    @Override
    public MyLibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyLibraryViewHolder(mInflater.inflate(R.layout.rv_library_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyLibraryViewHolder holder, int position) {
        holder.setItemData(mList.get(position));
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(),
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top));
        lastPosition = position;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyLibraryViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return mList.size();
        return 0;
    }

    /*
     * Custom View holder class
     */
    static class MyLibraryViewHolder extends RecyclerView.ViewHolder {

        private TextView songName, artist;
        private ImageView albumArt;

        MyLibraryViewHolder(View itemView, LibraryItemClickListener listener) {
            super(itemView);
            songName = itemView.findViewById(R.id.library_item_tv1);
            artist = itemView.findViewById(R.id.library_item_tv2);
            albumArt = itemView.findViewById(R.id.library_item_iv1);

            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));

            itemView.findViewById(R.id.library_item_iv2).setOnClickListener(v -> v.post(() -> listener.onOptionsClick(getAdapterPosition())));
        }

        void setItemData(MusicModel md) {
            songName.setText(md.getSongName());
            artist.setText(md.getArtist());
            GlideApp.with(itemView)
                    .load(md.getAlbumArtUrl())
                    .error(R.drawable.ic_album_art)
                    .transform(GlideConstantArtifacts.getRoundingRadiusSmall())
                    .into(albumArt);
        }
    }
}
