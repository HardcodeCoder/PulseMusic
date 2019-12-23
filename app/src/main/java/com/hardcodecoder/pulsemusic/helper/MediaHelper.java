package com.hardcodecoder.pulsemusic.helper;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;

import com.hardcodecoder.pulsemusic.model.MusicModel;

public class MediaHelper {

    public static MusicModel buildMusicModelFrom(Context context, Intent data){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, data.getData());
        try{
            String path = data.getDataString();
            if(path != null ) {
                String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                int duration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                mmr.release();
                return new MusicModel(-1, name, artist, path, album, -1, null, duration);
            }
            return null;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

