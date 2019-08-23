package com.hardcodecoder.pulsemusic.helper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.interfaces.RecyclerViewGestures;

public class RecyclerViewGestureHelper extends ItemTouchHelper.Callback {

    private RecyclerViewGestures.GestureCallback mCompletionCallback;

    public RecyclerViewGestureHelper(@NonNull RecyclerViewGestures.GestureCallback callback) {
        mCompletionCallback = callback;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mCompletionCallback.onItemSwiped(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mCompletionCallback.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
        mCompletionCallback.onItemMoved(fromPos, toPos);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        mCompletionCallback.onClearView(recyclerView, viewHolder);
        super.clearView(recyclerView, viewHolder);
    }
}
