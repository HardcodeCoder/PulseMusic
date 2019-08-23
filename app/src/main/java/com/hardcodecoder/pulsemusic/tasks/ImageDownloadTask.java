package com.hardcodecoder.pulsemusic.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider;

import java.io.InputStream;

public class ImageDownloadTask extends AsyncTask<Void, Void, Bitmap> {


    private CompletionCallback mCallback;

    public ImageDownloadTask(CompletionCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        String urlDisplay = HomeWalliProvider.getUrl();
        Bitmap image = null;
        try {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mCallback.imageFetchCompleted(bitmap);
    }

    public interface CompletionCallback {
        void imageFetchCompleted(Bitmap bitmap);
    }
}
