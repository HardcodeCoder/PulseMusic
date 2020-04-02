package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.AlbumModel;

import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsSVH> {

    private List<AlbumModel> data;
    private ItemClickListener.SingleEvent mListener;
    private LayoutInflater mInflater;

    public AlbumsAdapter(List<AlbumModel> list, LayoutInflater mInflater, ItemClickListener.SingleEvent mListener) {
        this.mInflater = mInflater;
        this.mListener = mListener;
        this.data = list;
    }

    @NonNull
    @Override
    public AlbumsSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumsSVH(mInflater.inflate(R.layout.rv_grid_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsSVH holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        if (null != data)
            return data.size();
        return 0;
    }

    static class AlbumsSVH extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView art;

        AlbumsSVH(@NonNull View itemView, ItemClickListener.SingleEvent mListener) {
            super(itemView);
            art = itemView.findViewById(R.id.grid_item_iv);
            title = itemView.findViewById(R.id.grid_item_tv);
            itemView.setOnClickListener(v -> mListener.onClickItem(getAdapterPosition()));
        }

        void setData(AlbumModel am) {
            GlideApp
                    .with(itemView)
                    .load(am.getAlbumArt())
                    .error(R.drawable.ic_album_art)
                    .transform(GlideConstantArtifacts.getDefaultRoundingRadius())
                    .into(art);

            title.setText(am.getAlbumName());
        }
    }
}
