package com.hardcodecoder.pulsemusic.utils;

import android.content.Context;
import android.util.Log;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaylistStorageManager {

    //private static final String RECENT_TRACKS_FILE_NAME = "Recent_Tracks.data";
    private static final String FOLDER_HISTORY = "/history/";
    private static final String PLAYLIST_DATA_FILE_NAME = "Playlist_Tracks_Data.data";
    private static final String PLAYLIST_TITLE_FILE_NAME = "Playlist_Names.data";
    private static final String FAVORITE_TRACKS_FILE_NAME = "FavoriteTracks.data";

    private PlaylistStorageManager() {
    }

    /*public static void saveRecentTracks(Context mContext, List<MusicModel> newHistory) {
        if (null != newHistory && newHistory.size() > 0) {
            FileOutputStream outputStream;
            try {
                outputStream = mContext.openFileOutput(RECENT_TRACKS_FILE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(newHistory);
                objectOutputStream.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<MusicModel> getRecentTracks(Context mContext) {
        FileInputStream inputStream;
        List<MusicModel> list = new ArrayList<>();
        try {
            inputStream = mContext.openFileInput(RECENT_TRACKS_FILE_NAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            list = ((List<MusicModel>) objectInputStream.readObject());
            objectInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            LogHelper(RECENT_TRACKS_FILE_NAME);
        }
        return list;
    }*/


    public static void addToRecentTracks(Context context, MusicModel md){
        File f = new File(context.getFilesDir().getAbsoluteFile() + FOLDER_HISTORY + md.getSongName() + ".history");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(md);
            objectOutputStream.close();
            fileOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<MusicModel> getRecentTracks(Context context){
        File f = new File(context.getFilesDir().getAbsoluteFile() + FOLDER_HISTORY);
        File[] files = f.listFiles();
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        List<MusicModel> list = new ArrayList<>();
        if(null != files) {
            Arrays.sort(files, (o1, o2) -> (int)(o2.lastModified() - o1.lastModified()));
            for (File file : files) {
                try {
                    fileInputStream = new FileInputStream(file);
                    objectInputStream = new ObjectInputStream(fileInputStream);
                    list.add((MusicModel) objectInputStream.readObject());
                    objectInputStream.close();
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static void saveFavorite(Context mContext, List<MusicModel> list) {
        if (null != list && list.size() > 0) {
            FileOutputStream outputStream;
            try {
                outputStream = mContext.openFileOutput(FAVORITE_TRACKS_FILE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(list);
                objectOutputStream.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File f = new File(mContext.getFilesDir(), FAVORITE_TRACKS_FILE_NAME);
            if (f.delete())
                Log.w("PlaylistStorageManager", "Favorites playlist deleted");
        }
    }

    @SuppressWarnings("unchecked")
    public static List<MusicModel> getFavorite(Context context) {
        FileInputStream inputStream;
        List<MusicModel> list = new ArrayList<>();
        try {
            inputStream = context.openFileInput(FAVORITE_TRACKS_FILE_NAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            list = ((List<MusicModel>) objectInputStream.readObject());
            objectInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            LogHelper(FAVORITE_TRACKS_FILE_NAME);
        }
        return list;
    }

    public static void savePlaylistTitles(Context mContext, List<String> titles) {
        FileOutputStream outputStream;
        try {
            outputStream = mContext.openFileOutput(PLAYLIST_TITLE_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(titles);
            objectOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            LogHelper(PLAYLIST_TITLE_FILE_NAME);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getPlaylistTitles(Context mContext) {
        FileInputStream inputStream;
        List<String> list = new ArrayList<>();
        try {
            inputStream = mContext.openFileInput(PLAYLIST_TITLE_FILE_NAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            list.clear();
            list.addAll((List<String>) objectInputStream.readObject());
            objectInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            LogHelper(PLAYLIST_TITLE_FILE_NAME);
        }
        if (list.size() == 0) {
            list.add(mContext.getString(R.string.playlist_current_queue));
            list.add(mContext.getString(R.string.favorite_playlist));
        }
        return list;
    }

    private static void savePlaylistTracksALl(Context mContext, List<List<MusicModel>> listOfLists) {
        FileOutputStream outputStream;
        try {
            outputStream = mContext.openFileOutput(PLAYLIST_DATA_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(listOfLists);
            objectOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            LogHelper(PLAYLIST_DATA_FILE_NAME);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<List<MusicModel>> getPlaylistTracksAll(Context mContext) {
        FileInputStream inputStream;
        List<List<MusicModel>> listOfList = new ArrayList<>();
        try {
            inputStream = mContext.openFileInput(PLAYLIST_DATA_FILE_NAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            listOfList = (List<List<MusicModel>>) objectInputStream.readObject();
            objectInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            LogHelper(PLAYLIST_DATA_FILE_NAME);
        }
        return listOfList;
    }

    public static void updatePlaylistTracks(Context mContext, List<MusicModel> updatedList, int index) {
        List<List<MusicModel>> listOfList = getPlaylistTracksAll(mContext);
        int l = listOfList.size();
        if (l > 0) {
            if (index < l) listOfList.remove(index);
            if (updatedList != null) listOfList.add(index, updatedList);
        } else listOfList.add(index, updatedList);

        savePlaylistTracksALl(mContext, listOfList);
    }

    public static List<MusicModel> getPlaylistTrackAtPosition(Context mContext, int pos) {
        List<List<MusicModel>> listOfList = getPlaylistTracksAll(mContext);
        List<MusicModel> mList = new ArrayList<>();
        if (pos < listOfList.size()) {
            mList.addAll(listOfList.get(pos));
            return mList;
        }
        else
            return mList;
    }

    public static void dropPlaylistCardDataAt(Context mContext, int pos) {
        updatePlaylistTracks(mContext, null, pos);
    }

    public static void dropAllPlaylistData(Context mContext) {
        try {
            File f = new File(mContext.getFilesDir(), PLAYLIST_TITLE_FILE_NAME);
            if (!f.delete())
                Log.e("PSM", "Unable to delete playlist title data file");
            f = new File(mContext.getFilesDir(), PLAYLIST_DATA_FILE_NAME);
            if (!f.delete())
                Log.e("PSM", "Unable to delete playlist list data file");
        } catch (Exception e) {
            LogHelper(PLAYLIST_TITLE_FILE_NAME + "and" + PLAYLIST_DATA_FILE_NAME);
        }
    }

    private static void LogHelper(String file) {
        Log.v("PlaylistStorageManager", "File not found : " + file);
    }

}
