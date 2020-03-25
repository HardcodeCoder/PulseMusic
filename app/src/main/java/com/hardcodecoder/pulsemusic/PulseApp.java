package com.hardcodecoder.pulsemusic;

import android.app.Application;

import com.hardcodecoder.pulsemusic.themes.ThemeManager;

public class PulseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.init(getApplicationContext());
        int radius = getResources().getDimensionPixelSize(R.dimen.rounding_radius_default);
        int small = getResources().getDimensionPixelSize(R.dimen.rounding_radius_small);
        GlideConstantArtifacts.init(new int[]{radius, small});
    }
}
