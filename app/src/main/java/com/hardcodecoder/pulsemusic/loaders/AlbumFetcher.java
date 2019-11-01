package com.hardcodecoder.pulsemusic.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
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
        if (sort == SORT.TITLE_ASC)
            mSort = MediaStore.Audio.Albums.ALBUM + " ASC";
        else if (sort == SORT.DATE_ASC)
            mSort = MediaStore.Audio.Albums.FIRST_YEAR + " ASC";
    }

    @Override
    protected List<AlbumModel> doInBackground(Void... voids) {
        String[] col = {MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS};
        final Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                col,
                null,
                null,
                mSort);

        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
            int albumColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            int albumIdColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID);
            int songCountColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            do {
                int id = cursor.getInt(idColumnIndex);
                String album = cursor.getString(albumColumnIndex);
                long albumId = cursor.getLong(albumIdColumnIndex);
                String albumArt = ContentUris.withAppendedId(sArtworkUri, albumId).toString();
                int num = cursor.getInt(songCountColumnIndex);

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
