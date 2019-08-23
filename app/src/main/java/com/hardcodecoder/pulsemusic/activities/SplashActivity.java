package com.hardcodecoder.pulsemusic.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.tasks.TrackFetcherFromStorage;
import com.hardcodecoder.pulsemusic.utils.UserInfo;

import java.util.List;


public class SplashActivity extends Activity implements TrackFetcherFromStorage.TaskDelegate {

    private static final int REQUEST_CODE = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserInfo.initSharedPrefs(this);
        setTheme(UserInfo.getThemeToApply());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getPermission();
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startMusicLoader();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMusicLoader();
            } else {
                // Permission was not granted
                Toast.makeText(this, "App needs to access device storage to work", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(this::finish, 1500);
            }
        }
    }

    @Override
    public void onTaskCompleted(List<MusicModel> list) {
        TrackManager.getInstance().setMainList(list);
        startHomeActivity();
    }

    private void startHomeActivity() {
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }, 600);
    }

    private void startMusicLoader() {
        TrackFetcherFromStorage ml = new TrackFetcherFromStorage(getContentResolver(), this, TrackFetcherFromStorage.Sort.TITLE_ASC);
        ml.execute();
    }
}

