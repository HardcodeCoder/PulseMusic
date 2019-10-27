package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.ArtistModel;

import java.util.List;

public class HomeAdapterArtist extends RecyclerView.Adapter<HomeAdapterArtist.AdapterSVH> {

    private List<ArtistModel> mList;
    private ItemClickListener.Simple mListener;
    private LayoutInflater mInflater;

    public HomeAdapterArtist(LayoutInflater inflater, List<ArtistModel> list, ItemClickListener.Simple listener) {
        this.mInflater = inflater;
        this.mList = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdapterSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterSVH(mInflater.inflate(R.layout.rv_home_item_artist, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSVH holder, int position) {
        holder.updateData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        if (null != mList)
            return mList.size();
        return 0;
    }

    static class AdapterSVH extends RecyclerView.ViewHolder {

        private TextView artistName, trackCount;

        AdapterSVH(@NonNull View itemView, ItemClickListener.Simple listener) {
            super(itemView);
            artistName = itemView.findViewById(R.id.rv_item_title);
            trackCount = itemView.findViewById(R.id.rv_item_artist);
            itemView.setOnClickListener(v -> listener.onOptionsClick(itemView.findViewById(R.id.artist_home), getAdapterPosition()));
        }

        void updateData(ArtistModel am) {
            artistName.setText(am.getArtistName());
            trackCount.setText(String.format("%d " + itemView.getResources().getString(R.string.tracks_num ), am.getNumOfTracks()));
        }
    }
}
