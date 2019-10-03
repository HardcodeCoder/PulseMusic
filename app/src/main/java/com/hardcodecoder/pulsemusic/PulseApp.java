package com.hardcodecoder.pulsemusic;

import android.app.Application;

import com.hardcodecoder.pulsemusic.themes.ThemeManager;

public class PulseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.init(getApplicationContext());
    }
}
