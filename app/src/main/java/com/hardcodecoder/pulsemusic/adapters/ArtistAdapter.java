package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.TransitionClickListener;
import com.hardcodecoder.pulsemusic.model.ArtistModel;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistSVH> {
    private List<ArtistModel> data;
    private TransitionClickListener mListener;
    private LayoutInflater mInflater;

    public ArtistAdapter(List<ArtistModel> list, LayoutInflater mInflater, TransitionClickListener mListener) {
        this.mInflater = mInflater;
        this.mListener = mListener;
        this.data = list;
    }

    @NonNull
    @Override
    public ArtistAdapter.ArtistSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistSVH(mInflater.inflate(R.layout.rv_grid_item_artist, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistSVH holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        if (null != data)
            return data.size();
        return 0;
    }

    static class ArtistSVH extends RecyclerView.ViewHolder {

        private TextView title;

        ArtistSVH(@NonNull View itemView, TransitionClickListener mListener) {
            super(itemView);
            title = itemView.findViewById(R.id.grid_item_artist_tv);
            itemView.setOnClickListener(v -> mListener.onItemClick(itemView, getAdapterPosition()));
        }

        void setData(ArtistModel am) {
            title.setText(am.getArtistName());
        }
    }
}
