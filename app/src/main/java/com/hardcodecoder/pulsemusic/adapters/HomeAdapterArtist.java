package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class HomeAdapterArtist extends RecyclerView.Adapter<HomeAdapterArtist.AdapterSVH> {

    private List<MusicModel> mList;
    private ItemClickListener.Simple mListener;
    private LayoutInflater mInflater;

    public HomeAdapterArtist(LayoutInflater inflater, List<MusicModel> list, ItemClickListener.Simple listener) {
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

        private TextView artistName, trackTitle;

        AdapterSVH(@NonNull View itemView, ItemClickListener.Simple listener) {
            super(itemView);
            artistName = itemView.findViewById(R.id.rv_item_title);
            trackTitle = itemView.findViewById(R.id.rv_item_artist);
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
            itemView.setOnLongClickListener(v -> {
                listener.onOptionsClick(itemView, getAdapterPosition());
                return true;
            });
        }

        void updateData(MusicModel md) {
            artistName.setText(md.getArtist());
            trackTitle.setText(md.getSongName());
        }
    }
}
