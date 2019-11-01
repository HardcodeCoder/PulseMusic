package com.hardcodecoder.pulsemusic.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.ArrayList;
import java.util.List;

public class TrackFetcherFromStorage extends AsyncTask<Void, Void, List<MusicModel>> {

    private ContentResolver contentResolver;
    private TaskDelegate mtd;
    private String mSortOrder;
    private int itemToScan;


    public TrackFetcherFromStorage(ContentResolver contentResolver, TaskDelegate td, Sort sort) {
        this.contentResolver = contentResolver;
        mtd = td;
        if (sort == Sort.TITLE_ASC) {
            mSortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            itemToScan = -1; // Scans all media items
        } else if (sort == Sort.DATE_ADDED_DESC) {
            mSortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
            itemToScan = 20; // Scans only 20 items
        }
    }

    @Override
    protected List<MusicModel> doInBackground(Void... voids) {
        int c = 0;
        List<MusicModel> allSongs = new ArrayList<>();
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursor_cols = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                //MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        final Cursor cursor = contentResolver.query(
                uri,
                cursor_cols,
                null, //getSelection(),
                null, //getSelectionArgs(),
                mSortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int albumIdColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int durationColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

            do {
                if (c == itemToScan) //if itemToScan == -1 then this will never execute since @link{id} is a +ve integer
                    break;
                c++;

                int _id = cursor.getInt(idColumnIndex);
                String songName = cursor.getString(titleColumnIndex);
                String artist = cursor.getString(artistColumnIndex);
                String album = cursor.getString(albumColumnIndex);
                String songPath = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id).toString();
                long albumId = cursor.getLong(albumIdColumnIndex);
                int duration = cursor.getInt(durationColumnIndex);
                String albumArt = ContentUris.withAppendedId(sArtworkUri, albumId).toString();

                allSongs.add(new MusicModel(_id, songName, artist, songPath, album, albumId, albumArt, duration));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return allSongs;
    }

    private String getSelection() {
        return "("
                + "(" + MediaStore.Audio.Media.IS_MUSIC + "==? )"
                + "AND (" + MediaStore.Audio.Media.IS_ALARM + "==? )"
                + "AND (" + MediaStore.Audio.Media.IS_NOTIFICATION + "==? )"
                + "AND (" + MediaStore.Audio.Media.IS_PODCAST + "==? )"
                + "AND (" + MediaStore.Audio.Media.IS_RINGTONE + "==? )"
                + ")";
    }

    private String[] getSelectionArgs() {
        return new String[]{"1", "0", "0", "0", "0"};
    }

    @Override
    protected void onPostExecute(List<MusicModel> loadedList) {
        mtd.onTaskCompleted(loadedList);
    }

    public interface TaskDelegate {
        void onTaskCompleted(List<MusicModel> list);
    }

    public enum Sort {
        TITLE_ASC,
        DATE_ADDED_DESC,
    }
}
