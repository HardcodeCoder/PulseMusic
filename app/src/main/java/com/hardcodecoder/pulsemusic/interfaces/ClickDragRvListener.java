package com.hardcodecoder.pulsemusic.interfaces;

import androidx.recyclerview.widget.RecyclerView;

public interface ClickDragRvListener {
    /**
     * called when a recycler view item is clicked
     *
     * @param position gives the position of the item
     */
    void onItemClick(int position);

    /**
     * Called when the user touches the reorder button and drags
     *
     * @param holder is required for @Link ItemTouchListener#onStartDrag to work
     */
    void initiateDrag(RecyclerView.ViewHolder holder);

}
