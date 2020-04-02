package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.AlbumModel;

import java.util.List;

public class HomeAdapterAlbum extends RecyclerView.Adapter<HomeAdapterAlbum.AdapterSVH> {

    private LayoutInflater mInflater;
    private List<AlbumModel> mList;
    private ItemClickListener.Simple mListener;

    public HomeAdapterAlbum(LayoutInflater inflater, List<AlbumModel> list, ItemClickListener.Simple listener) {
        this.mInflater = inflater;
        this.mList = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public HomeAdapterAlbum.AdapterSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeAdapterAlbum.AdapterSVH(mInflater.inflate(R.layout.rv_album_card_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapterAlbum.AdapterSVH holder, int position) {
        holder.updateData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        if (null != mList)
            return mList.size();
        return 0;
    }

    static class AdapterSVH extends RecyclerView.ViewHolder {

        private ImageView iv;
        private TextView tv;

        AdapterSVH(@NonNull View itemView, ItemClickListener.Simple listener) {
            super(itemView);
            tv = itemView.findViewById(R.id.rv_item_title);
            iv = itemView.findViewById(R.id.iv_album_card);
            itemView.setOnClickListener(v -> listener.onOptionsClick(iv, getAdapterPosition()));
        }

        void updateData(AlbumModel am) {
            GlideApp.with(iv)
                    .load(am.getAlbumArt())
                    .transform(new CenterCrop(), GlideConstantArtifacts.getDefaultRoundingRadius())
                    .error(R.drawable.ic_album_art)
                    .transition(GenericTransitionOptions.with(R.anim.fade_in_image))
                    .into(iv);
            tv.setText(am.getAlbumName());
        }
    }
}
