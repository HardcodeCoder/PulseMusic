package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.GenericTransitionOptions;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private List<MusicModel> mList;
    private ItemClickListener.Simple mListener;
    @LayoutRes
    private int mLayout;
    private LayoutStyle mLayoutStyle;
    private LayoutInflater mInflater;

    public HomeAdapter(LayoutInflater inflater, List<MusicModel> list, ItemClickListener.Simple clickListener, LayoutStyle style) {
        this.mListener = clickListener;
        this.mList = list;
        this.mLayoutStyle = style;
        this.mInflater = inflater;
        if (style == LayoutStyle.CIRCLE) mLayout = R.layout.rv_home_item_cir;
        else mLayout = R.layout.rv_home_item_sq;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(mLayout, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setItemData(mList.get(position), mLayoutStyle);
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
    static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView songName, artist;
        private ImageView albumArt;

        MyViewHolder(View itemView, ItemClickListener.Simple listener) {
            super(itemView);
            songName = itemView.findViewById(R.id.rv_item_title);
            artist = itemView.findViewById(R.id.rv_item_artist);
            albumArt = itemView.findViewById(R.id.rv_item_album_art);
            itemView.setOnLongClickListener(v -> {
                listener.onOptionsClick(v, getAdapterPosition());
                return true;
            });
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
        }

        void setItemData(MusicModel md, LayoutStyle style) {
            songName.setText(md.getSongName());
            artist.setText(md.getArtist());
            GlideApp.with(albumArt)
                    .load(md.getAlbumArtUrl())
                    .error(R.drawable.ic_album_art)
                    .transform(style == LayoutStyle.ROUNDED_RECTANGLE ?
                            GlideConstantArtifacts.getDefaultRoundingRadius() : GlideConstantArtifacts.getCircleCrop())
                    .transition(GenericTransitionOptions.with(R.anim.fade_in_image))
                    .into(albumArt);
        }
    }

    public enum LayoutStyle {
        CIRCLE,
        ROUNDED_RECTANGLE
    }
}
