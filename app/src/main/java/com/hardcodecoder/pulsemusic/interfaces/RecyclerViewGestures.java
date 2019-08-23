package com.hardcodecoder.pulsemusic.interfaces;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerViewGestures {

    interface GestureCallback {
        void onItemSwiped(int itemAdapterPosition);

        void onItemMove(int fromPosition, int toPosition);

        void onItemMoved(int fromPos, int toPos);

        /**
         * Callback to clear background of selected item view
         * and restore default background
         *
         * @param recyclerView the recycler view
         * @param viewHolder   the item view holder
         */
        void onClearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder);
    }
}
