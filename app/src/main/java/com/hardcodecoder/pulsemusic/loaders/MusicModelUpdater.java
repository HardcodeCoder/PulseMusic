package com.hardcodecoder.pulsemusic.loaders;

import android.os.AsyncTask;

import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.util.List;

public class MusicModelUpdater extends AsyncTask<Void, Void, List<MusicModel>> {

    private List<MusicModel> mOldList;
    private AsyncTaskCallback.UpdateCompletion mCallback;
    private boolean flag = false;
    private boolean modified;

    public MusicModelUpdater(List<MusicModel> oldList, AsyncTaskCallback.UpdateCompletion callback) {
        mOldList = oldList;
        mCallback = callback;
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        List<MusicModel> master = TrackManager.getInstance().getMainList();
        MusicModel md1;
        if (null != master && master.size() > 0 && null != mOldList) {
            for (int i = 0; i < mOldList.size(); i++) {
                md1 = mOldList.get(i);
                String title = md1.getSongName();
                for (MusicModel md2 : master) {
                    /*if (title.equals(md2.getSongName())) {
                        if(!md1.getSongPath().equals(md2.getSongPath())) {
                            md1.setSongPath(md2.getSongPath());
                            md1.setAlbumArtUrl(md2.getAlbumArtUrl());
                            flag = true;
                        }
                    }*/

                    if (md2.getSongName().equals(title)) {
                        flag = true;
                        if (!md1.getSongPath().equals(md2.getSongPath())) {
                            md1.setSongPath(md2.getSongPath());
                            modified = true;
                        }
                        break;
                    }
                }

                if (!flag) mOldList.remove(md1);
            }
        } else mOldList = null;

        return mOldList;
    }

    @Override
    protected void onPostExecute(List<MusicModel> musicModels) {
        mCallback.onUpdateComplete(musicModels, modified);
    }
}
