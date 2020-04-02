package com.hardcodecoder.pulsemusic.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.interfaces.ClickDragRvListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class PlaylistDataAdapter extends RecyclerView.Adapter<PlaylistDataAdapter.PlaylistDataSVH> {

    private ClickDragRvListener mListener;
    private LayoutInflater mInflater;
    private List<MusicModel> mList;
    private int deletedPosition;

    public PlaylistDataAdapter(ClickDragRvListener mListener, LayoutInflater inflater) {
        this.mListener = mListener;
        this.mInflater = inflater;
    }

    public void removeItem(int pos) {
        deletedPosition = pos;
        notifyItemRemoved(pos);
    }

    public void restoreItem() {
        notifyItemInserted(deletedPosition);
    }

    @NonNull
    @Override
    public PlaylistDataSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistDataSVH(mInflater.inflate(R.layout.rv_playlist_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistDataSVH holder, int position) {
        holder.updateView(mList.get(position));
    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return mList.size();
        else
            return 0;
    }

    // The Fragment or Activity will call this method
    // when new data becomes available
    public void addItems(List<MusicModel> list) {
        if (list.size() > 0) {
            int oldSize = 0;
            if (mList != null)
                oldSize = mList.size();
            mList = list;
            notifyItemRangeInserted(oldSize, mList.size() - 1);
        }
    }

    static class PlaylistDataSVH extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView art;

        PlaylistDataSVH(@NonNull View itemView, ClickDragRvListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.library_item_tv1);
            art = itemView.findViewById(R.id.library_item_iv1);
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
            //noinspection AndroidLintClickableViewAccessibility
            itemView.findViewById(R.id.btn_handle)
                    .setOnTouchListener((v, event) -> {
                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_UP)
                            listener.initiateDrag(this);
                        return true;
                    });
        }

        void updateView(MusicModel md) {
            title.setText(md.getSongName());
            GlideApp
                    .with(itemView.getContext())
                    .load(md.getAlbumArtUrl())
                    .error(R.drawable.ic_album_art)
                    .transform(GlideConstantArtifacts.getDefaultRoundingRadius())
                    .into(art);
        }
    }
}
