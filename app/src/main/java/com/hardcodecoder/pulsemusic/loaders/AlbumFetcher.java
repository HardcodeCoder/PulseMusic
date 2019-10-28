package com.hardcodecoder.pulsemusic.loaders;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.hardcodecoder.pulsemusic.interfaces.AlbumDataFetchCompletionCallback;
import com.hardcodecoder.pulsemusic.model.AlbumModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumFetcher extends AsyncTask<Void, Void, List<AlbumModel>> {

    private AlbumDataFetchCompletionCallback mCallback;
    private List<AlbumModel> data = new ArrayList<>();
    private ContentResolver mContentResolver;
    private String mSort;

    public AlbumFetcher(ContentResolver mContentResolver, AlbumDataFetchCompletionCallback mCallback, SORT sort) {
        this.mCallback = mCallback;
        this.mContentResolver = mContentResolver;
        if(sort == SORT.TITLE_ASC)
            mSort = MediaStore.Audio.Albums.ALBUM + " ASC";
        else if(sort == SORT.DATE_ASC)
            mSort = MediaStore.Audio.Albums.FIRST_YEAR + " ASC";
    }

    @Override
    protected List<AlbumModel> doInBackground(Void... voids) {
        String[] col = {MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS};
        final Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                col,
                null,
                null,
                mSort);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));

                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));

                String albumArt = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));

                int num = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS));

                data.add(new AlbumModel(id, album, num, albumArt));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return data;
    }

    @Override
    protected void onPostExecute(List<AlbumModel> albumModels) {
        mCallback.onTaskComplete(albumModels);
    }

    public enum SORT {
        TITLE_ASC,
        DATE_ASC
    }

}
