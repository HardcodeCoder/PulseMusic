package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;

import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardsSVH> {

    private List<String> nameOfPlaylist;
    private ItemClickListener.Cards mListener;
    private LayoutInflater mInflater;

    public CardsAdapter(List<String> nameOfPlaylist, ItemClickListener.Cards mListener, LayoutInflater inflater) {
        this.nameOfPlaylist = nameOfPlaylist;
        this.mListener = mListener;
        this.mInflater = inflater;
    }

    @NonNull
    @Override
    public CardsSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardsSVH(mInflater.inflate(R.layout.rv_playlist_card_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CardsSVH holder, int position) {
        holder.updateView(nameOfPlaylist.get(position));
    }

    @Override
    public int getItemCount() {
        if (nameOfPlaylist != null)
            return nameOfPlaylist.size();
        else return 0;
    }

    static class CardsSVH extends RecyclerView.ViewHolder {

        private TextView title;

        CardsSVH(@NonNull View itemView, ItemClickListener.Cards listener) {
            super(itemView);
            title = itemView.findViewById(R.id.playlist_title);

            itemView.findViewById(R.id.edit_btn).setOnClickListener(v -> listener.onEdit(getAdapterPosition()));

            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
        }

        void updateView(String s) {
            title.setText(s);
            if (getAdapterPosition() <= 1)
                itemView.findViewById(R.id.edit_btn).setVisibility(View.GONE);
        }
    }
}
