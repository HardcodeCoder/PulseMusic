package com.hardcodecoder.pulsemusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hardcodecoder.pulsemusic.themes.ThemeStore;

public class AppSettings {

    private static final String TAG = "AppSettings";
    private static final String SPAN_COUNT_PORTRAIT = "PortraitGridCount";
    private static final String SPAN_COUNT_LANDSCAPE = "LandscapeGridCount";
    private static final String UI_MODE_AUTO = "AutoTheme";
    private static final String UI_THEME_DARK = "DarkMode";

    public static void savePortraitGridSpanCount(@Nullable Context context, int count) {
        if(null != context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(SPAN_COUNT_PORTRAIT, Context.MODE_PRIVATE).edit();
            editor.putInt(SPAN_COUNT_PORTRAIT, count);
            editor.apply();
        }
        else Log.e(TAG, "Context is null cannot save data to shared preference");
    }

    public static int getPortraitGridSpanCount(@Nullable Context c) {
        if(null == c)
            return 2;
        return c.getSharedPreferences(SPAN_COUNT_PORTRAIT, Context.MODE_PRIVATE).getInt(SPAN_COUNT_PORTRAIT, 2);
    }

    public static void saveLandscapeGridSpanCount(@Nullable Context context, int count) {
        if(null != context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(SPAN_COUNT_LANDSCAPE, Context.MODE_PRIVATE).edit();
            editor.putInt(SPAN_COUNT_LANDSCAPE, count);
            editor.apply();
        }
        else Log.e(TAG, "Context is null cannot save data to shared preference");
    }

    public static int getLandscapeGridSpanCount(@Nullable Context c) {
        if(null == c)
            return 4;
        return c.getSharedPreferences(SPAN_COUNT_LANDSCAPE, Context.MODE_PRIVATE).getInt(SPAN_COUNT_LANDSCAPE, 4);
    }

    public static void enableDarkMode(Context context, boolean state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(UI_THEME_DARK, Context.MODE_PRIVATE).edit();
        editor.putBoolean(UI_THEME_DARK, state);
        editor.apply();
    }

    public static void enableAutoTheme(Context context, boolean state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(UI_MODE_AUTO, Context.MODE_PRIVATE).edit();
        editor.putBoolean(UI_MODE_AUTO, state);
        editor.apply();
    }

    public static boolean isDarkModeEnabled(Context context){
        return context.getSharedPreferences(UI_THEME_DARK, Context.MODE_PRIVATE).getBoolean(UI_THEME_DARK, false);
    }

    public static boolean isAutoThemeEnabled(Context context){
        return context.getSharedPreferences(UI_MODE_AUTO, Context.MODE_PRIVATE).getBoolean(UI_MODE_AUTO, false);
    }

    public static void saveSelectedLightTheme(Context context, int id) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ThemeStore.LIGHT_THEME_CATEGORY, Context.MODE_PRIVATE).edit();
        editor.putInt(ThemeStore.LIGHT_THEME_CATEGORY, id);
        editor.apply();
    }

    public static void saveSelectedDarkTheme(Context context, int id) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ThemeStore.DARK_THEME_CATEGORY, Context.MODE_PRIVATE).edit();
        editor.putInt(ThemeStore.DARK_THEME_CATEGORY, id);
        editor.apply();
    }

    public static int getSelectedLightTheme(Context context) {
        return context.getSharedPreferences(ThemeStore.LIGHT_THEME_CATEGORY, Context.MODE_PRIVATE)
                .getInt(ThemeStore.LIGHT_THEME_CATEGORY, ThemeStore.LIGHT_THEME_1);
    }

    public static int getSelectedDarkTheme(Context context) {
        return context.getSharedPreferences(ThemeStore.DARK_THEME_CATEGORY, Context.MODE_PRIVATE)
                .getInt(ThemeStore.DARK_THEME_CATEGORY, ThemeStore.DARK_THEME_1);
    }
}
