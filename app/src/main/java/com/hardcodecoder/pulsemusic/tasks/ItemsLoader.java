package com.hardcodecoder.pulsemusic.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hardcodecoder.pulsemusic.activities.DetailsActivity;
import com.hardcodecoder.pulsemusic.interfaces.AsyncTaskCallback;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;

public class ItemsLoader extends AsyncTask<Void, Void, List<MusicModel>> {

    private AsyncTaskCallback.Simple mCallback;
    private String title;
    private int choice;


    public ItemsLoader(AsyncTaskCallback.Simple mCallback, String itemToSearch, int choice) {
        this.mCallback = mCallback;
        this.title = itemToSearch;
        this.choice = choice;
    }

    @Override
    protected void onPostExecute(List<MusicModel> itemsModels) {
        mCallback.onTaskComplete(itemsModels);
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        List<MusicModel> listToWorkOn = TrackManager.getInstance().getMainList();
        List<MusicModel> listToReturn = null;
        if (null != listToWorkOn) {
            listToReturn = new ArrayList<>();
            for (MusicModel md : listToWorkOn) {

                if (choice == DetailsActivity.CATEGORY_ALBUM) {
                    if (md.getAlbum().contains(title) || md.getAlbum().equals(title))
                        listToReturn.add(md);
                } else if (choice == DetailsActivity.CATEGORY_ARTIST) {
                    if (md.getArtist().contains(title) || md.getArtist().equals(title))
                        listToReturn.add(md);
                }

            }
            if (listToReturn.size() == 0)
                Log.e("ItemsLoader", "Zero item found");
        }
        return listToReturn;
    }
}
