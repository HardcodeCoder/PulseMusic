package com.hardcodecoder.pulsemusic.helper;

import android.os.Build;
import android.view.View;
import android.view.Window;

public class StatusBarColorUpdater {

    private StatusBarColorUpdater() {
    }

    private static boolean checkBuildVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void adaptStatusBarForDarkBackground(Window window) {
        if (checkBuildVersion()) {
            View v = window.getDecorView();
            int flags = v.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            v.setSystemUiVisibility(flags);
        }
    }

    public static void adaptStatusBarForLightBackground(Window window) {
        if (checkBuildVersion())
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

}
