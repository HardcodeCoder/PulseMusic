package com.hardcodecoder.pulsemusic.helper;

import androidx.recyclerview.widget.DiffUtil;

import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class DiffCb extends DiffUtil.Callback {

    private List<MusicModel> oldItems;
    private List<MusicModel> newItems;

    public DiffCb(List<MusicModel> oldItems, List<MusicModel> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return oldItems.size();
    }

    @Override
    public int getNewListSize() {
        return newItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItems.get(oldItemPosition).equals(newItems.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItems.get(oldItemPosition).getSongName().equals(newItems.get(newItemPosition).getSongName());
    }
}
