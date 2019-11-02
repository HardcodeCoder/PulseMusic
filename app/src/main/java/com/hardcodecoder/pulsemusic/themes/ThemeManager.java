package com.hardcodecoder.pulsemusic.themes;

import android.content.Context;

import androidx.annotation.StyleRes;

import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider;
import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider.Day;
import com.hardcodecoder.pulsemusic.utils.AppSettings;

public class ThemeManager {

    private static boolean mDarkMode = false;
    private static boolean mAuto = false;
    private static int mThemeId;
    private static int mAccentId;

    public static void init(Context context){
        mAuto = AppSettings.isAutoThemeEnabled(context);
        if (mAuto) {
            Day day = HomeWalliProvider.getTimeOfDay();
            mDarkMode = (day == Day.EVENING || day == Day.NIGHT);
        } else mDarkMode = AppSettings.isDarkModeEnabled(context);

        if(mDarkMode) mThemeId = AppSettings.getSelectedDarkTheme(context);
        else mThemeId = ThemeStore.LIGHT_THEME_1;//AppSettings.getSelectedLightTheme(context);
        mAccentId = AppSettings.getSelectedAccentColor(context);
    }

    public static boolean isAutoThemeEnabled() {
        return mAuto;
    }

    public static boolean isDarkModeEnabled() {
        return mDarkMode;
    }

    public static void enableDarkMode(Context context, boolean enable){
        AppSettings.enableDarkMode(context, enable);
    }

    public static void enableAutoTheme(Context context, boolean enable) {
        AppSettings.enableAutoTheme(context, enable);
    }

    /*public static void setSelectedLightTheme(Context context, int id) {
        mThemeId = id;
        AppSettings.saveSelectedLightTheme(context, mThemeId);
    }*/

    public static void setSelectedDarkTheme(Context context, int id) {
        mThemeId = id;
        AppSettings.saveSelectedDarkTheme(context, mThemeId);
    }

    public static void setSelectedAccentColor(Context context, int id) {
        mAccentId = id;
        AppSettings.saveSelectedAccentColor(context, id);
    }

    @StyleRes
    public static int getThemeToApply() {
        return ThemeStore.getThemeById(mDarkMode, mThemeId);
    }

    @StyleRes
    public static int getAccentToApply() { return ThemeStore.getAccentById(mDarkMode, mAccentId); }
}
