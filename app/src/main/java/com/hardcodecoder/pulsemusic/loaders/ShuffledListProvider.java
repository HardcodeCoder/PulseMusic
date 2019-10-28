package com.hardcodecoder.pulsemusic.loaders;

import android.os.AsyncTask;

import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShuffledListProvider extends AsyncTask<Void, Void, List<MusicModel>> {


    private List<MusicModel> listToReturn = new ArrayList<>();
    private AsyncTaskCallback.Simple mCallback;

    public ShuffledListProvider(AsyncTaskCallback.Simple callback) {
        mCallback = callback;
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        return suggested();
    }

    private List<MusicModel> suggested() {
        List<MusicModel> listToWorkOn = TrackManager.getInstance().getMainList();
        if (null != listToWorkOn) {
            int size = listToWorkOn.size(), index;
            if (size > 0) {
                int i = 0;
                Random rn = new Random();
                while (i < 15) {
                    index = rn.nextInt(size);
                    if (index == size)
                        index = index - 1;
                    if (!listToReturn.contains(listToWorkOn.get(index))) {
                        listToReturn.add(listToWorkOn.get(index));
                        i++;
                    }
                }
            }
        }
        return listToReturn;
    }

    @Override
    protected void onPostExecute(List<MusicModel> musicModels) {
        mCallback.onTaskComplete(musicModels);
    }
}
