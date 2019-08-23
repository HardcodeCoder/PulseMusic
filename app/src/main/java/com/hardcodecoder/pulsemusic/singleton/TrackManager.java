package com.hardcodecoder.pulsemusic.singleton;

import android.content.Context;

import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.playback.PlaybackManager;
import com.hardcodecoder.pulsemusic.utils.PlaylistStorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TrackManager {

    private static final TrackManager ourInstance = new TrackManager();
    private List<MusicModel> mNewHistory = new ArrayList<>();
    private List<MusicModel> mActiveList = new ArrayList<>();
    private List<MusicModel> mMainList;
    private int mIndex = -1;
    private int mActiveListSize = -1;
    private MusicModel mDeletedQueueItem;
    private int mDeletedQueueIndex;
    private boolean mRepeatCurrentTrack = false;

    private TrackManager() {
    }

    public static TrackManager getInstance() {
        return ourInstance;
    }

    public void setMainList(final List<MusicModel> mainList) {
        mMainList = mainList;
    }

    public List<MusicModel> getMainList() {
        return mMainList;
    }

    public void buildDataList(List<MusicModel> newList, int index) {
        mActiveList.clear();
        mActiveList.addAll(newList);
        mActiveListSize = mActiveList.size();
        setActiveIndex(index);
    }

    private void setActiveIndex(int index) {
        mIndex = index;
    }

    public int getActiveIndex() {
        return mIndex;
    }

    public List<MusicModel> getActiveQueue() {
        return mActiveList;
    }

    public MusicModel getActiveQueueItem() {
        return mActiveList.get(mIndex);
    }

    public void updateActiveQueue(int from, int to) {
        Collections.swap(mActiveList, from, to);
    }

    public void repeatCurrentTrack(boolean b) {
        mRepeatCurrentTrack = b;
    }

    public boolean isCurrentTrackInRepeatMode() {
        return mRepeatCurrentTrack;
    }

    public boolean canSkipTrack(short direction) {
        if (mRepeatCurrentTrack) {
            mRepeatCurrentTrack = false;
            return true;
        }
        if (direction == PlaybackManager.ACTION_PLAY_NEXT && mIndex < mActiveListSize - 1) {
            setActiveIndex(++mIndex);
            return true;
        } else if (direction == PlaybackManager.ACTION_PLAY_PREV && mIndex > 0) {
            setActiveIndex(--mIndex);
            return true;
        } else {
            return false;
        }
    }

    public void playNext(MusicModel md) {
        if (mIndex + 1 < mActiveListSize) {
            if (mActiveList.get(mIndex + 1).getId() != md.getId()) {
                mActiveList.add(mIndex + 1, md);
                ++mActiveListSize;
            }
        } else {
            mActiveList.add(mIndex + 1, md);
            ++mActiveListSize;
        }

    }

    public void addToActiveQueue(MusicModel md) {
        mActiveList.add(md);
        ++mActiveListSize;
    }

    public boolean canRemoveItem(int position) {
        return position > -1 && position < mActiveListSize;
    }

    public void removeItemFromActiveQueue(int position) {
        if (position < mActiveListSize) {
            mDeletedQueueIndex = position;
            mDeletedQueueItem = mActiveList.remove(position);
        }
    }

    public void restoreItem() {
        mActiveList.add(mDeletedQueueIndex, mDeletedQueueItem);
    }

    /*
     * stores tracks that are played and saves them when app is closed
     */
    public List<MusicModel> getCurrentPlayedTracks() {
        return mNewHistory;
    }

    public void addToHistory() {
        mNewHistory.add(getActiveQueueItem());
    }

    public void saveTracks(Context mContext) {
        if (mNewHistory.size() > 0) {
            List<MusicModel> history = PlaylistStorageManager.getRecentTracks(mContext);
            if (null != history) mNewHistory.addAll(0, history);
            //mNewHistory = removeDuplicates();
            removeDuplicates();
            int s = mNewHistory.size();
            if (s > 15) mNewHistory = new ArrayList<>(mNewHistory.subList(s - 15, s));
            PlaylistStorageManager.saveRecentTracks(mContext, mNewHistory);

            /* newHistory has been saved to Storage so clear the list
             * So any further calls to saveTracks only saves the new tracks added
             * after previous saveTrack call
             * Also this prevents from passing already saved tracks via getCurrentPlayedTracks */
            mNewHistory.clear();
        }
    }

    private void removeDuplicates() {
        int size = mNewHistory.size(), lastIndex = size - 1;
        String name;
        HashMap<String, Boolean> hm = new HashMap<>();
        while (lastIndex >= 0) {
            name = mNewHistory.get(lastIndex).getSongName();
            if (hm.containsKey(name)) mNewHistory.remove(lastIndex);
            else hm.put(name, true);
            lastIndex--;
        }
    }
}
