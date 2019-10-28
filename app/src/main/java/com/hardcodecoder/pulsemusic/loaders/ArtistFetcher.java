package com.hardcodecoder.pulsemusic.loaders;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.hardcodecoder.pulsemusic.interfaces.ArtistDataFetchCompletionCallback;
import com.hardcodecoder.pulsemusic.model.ArtistModel;

import java.util.ArrayList;
import java.util.List;

public class ArtistFetcher extends AsyncTask<Void, Void, List<ArtistModel>> {

    private ArtistDataFetchCompletionCallback mCallback;
    private List<ArtistModel> data = new ArrayList<>();
    private ContentResolver mContentResolver;
    private String mSort;

    public ArtistFetcher(ContentResolver mContentResolver, ArtistDataFetchCompletionCallback mCallback, SORT sort) {
        this.mCallback = mCallback;
        this.mContentResolver = mContentResolver;
        if(sort == SORT.TITLE_ASC)
            mSort = MediaStore.Audio.Artists.ARTIST + " ASC";
        else if(sort == SORT.NUM_OF_TRACKS_DESC)
            mSort = MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " DESC";
    }

    @Override
    protected List<ArtistModel> doInBackground(Void... voids) {
        String[] col = {MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS};

        final Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                col,
                null,
                null,
                mSort);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));

                String artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));

                int num_albums = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));

                int num_tracks = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));

                data.add(new ArtistModel(id, artist, num_albums, num_tracks));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return data;
    }

    @Override
    protected void onPostExecute(List<ArtistModel> albumModels) {
        mCallback.onTaskComplete(albumModels);
    }

    public enum SORT {
        TITLE_ASC,
        NUM_OF_TRACKS_DESC
    }

}
