package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.LibraryItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.DetailsSVH> {

    private List<MusicModel> mList;
    private LibraryItemClickListener listener;
    private LayoutInflater mInflater;

    public DetailsAdapter(List<MusicModel> mList, LibraryItemClickListener listener, LayoutInflater mInflater) {
        this.mList = mList;
        this.listener = listener;
        this.mInflater = mInflater;
    }

    @NonNull
    @Override
    public DetailsSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetailsSVH(mInflater.inflate(R.layout.rv_details_item, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsSVH holder, int position) {
        holder.updateData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        if (null != mList)
            return mList.size();
        return 0;
    }

    static class DetailsSVH extends RecyclerView.ViewHolder {

        private TextView title;

        DetailsSVH(@NonNull View itemView, LibraryItemClickListener mListener) {
            super(itemView);
            title = itemView.findViewById(R.id.details_item_title);
            itemView.findViewById(R.id.details_iv_options).setOnClickListener(v -> mListener.onOptionsClick(getAdapterPosition()));
            itemView.setOnClickListener(v -> mListener.onItemClick(getAdapterPosition()));
        }

        void updateData(MusicModel md) {
            title.setText("\u2022 " + (getAdapterPosition() + 1) + "    " + md.getSongName());

        }
    }
}
