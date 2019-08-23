package com.hardcodecoder.pulsemusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.StyleRes;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider;
import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider.Day;

public class UserInfo {

    private static final String NAME_KEY = "userNameKey";
    private static final String AVATAR_KEY = "User profile pic";
    private static final String UI_MODE_AUTO = "ui_mode";
    private static final String UI_THEME_DARK = "ui_theme";
    private static final String SPAN_COUNT_PORTRAIT = "span_count_portrait";
    private static final String SPAN_COUNT_LANDSCAPE = "span_count_landscape";
    private static final String LIGHT_THEME_CATEGORY = "light_themes_key";
    private static final String DARK_THEME_CATEGORY = "dark_themes_key";
    public static final short LIGHT_THEME_1 = 515;
    public static final short LIGHT_THEME_2 = 525;
    public static final short LIGHT_THEME_3 = 535;
    public static final short DARK_THEME_1 = 616;
    public static final short DARK_THEME_2 = 626;
    public static final short DARK_THEME_3 = 636;
    private static SharedPreferences appSharedPrefs;
    private static boolean mDarkMode = false;
    private static boolean mAuto = false;
    @StyleRes
    private static int themeId;

    public static void initSharedPrefs(Context context) {
        appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mAuto = appSharedPrefs.getBoolean(UI_MODE_AUTO, false);
        if (mAuto) {
            Day day = HomeWalliProvider.getTimeOfDay();
            mDarkMode = (day == Day.EVENING || day == Day.NIGHT);
        } else mDarkMode = appSharedPrefs.getBoolean(UI_THEME_DARK, false);
        if (!mDarkMode) {
            int id = getSelectedLightTheme(context);
            switch (id) {
                case LIGHT_THEME_1:
                    themeId = R.style.ActivityThemeLight;
                    break;
                case LIGHT_THEME_2:
                    themeId = R.style.ActivityThemeBlackWhite;
                    break;
                case LIGHT_THEME_3:
                    themeId = R.style.ActivityThemeSweetMorning;
                    break;
                default:
                    themeId = R.style.ActivityThemeLight;
            }
        } else {
            int id = getSelectedDarkTheme(context);
            switch (id) {
                case DARK_THEME_1:
                    themeId = R.style.ActivityThemeDark;
                    break;
                case DARK_THEME_2:
                    themeId = R.style.ActivityThemeKindaDark;
                    break;
                case DARK_THEME_3:
                    themeId = R.style.ActivityThemeCharcoal;
                    break;
                default:
                    themeId = R.style.ActivityThemeDark;
            }
        }
    }

    public static void saveUserName(String name) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putString(NAME_KEY, name);
        editor.apply();
    }

    public static String getUserName() {
        if (null != appSharedPrefs)
            return appSharedPrefs.getString(NAME_KEY, "User");
        return "User";
    }

    public static void saveUserProfilePic(String path) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putString(AVATAR_KEY, path);
        editor.apply();
    }

    public static String getUserProfilePic() {
        if (null != appSharedPrefs)
            return appSharedPrefs.getString(AVATAR_KEY, "");
        return "";
    }

    public static void enableDarkMode(boolean state) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putBoolean(UI_THEME_DARK, state);
        editor.apply();
    }

    public static void enableAutoTheme(boolean state) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putBoolean(UI_MODE_AUTO, state);
        editor.apply();
    }

    public static boolean isAutoThemeEnabled() {
        return mAuto;
    }

    public static boolean isDarkModeEnabled() {
        return mDarkMode;
    }

    public static boolean isDarkModeSettingEnabled() {
        return appSharedPrefs.getBoolean(UI_THEME_DARK, false);
    }

    public static void savePortraitGridSpanCount(int count) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putInt(SPAN_COUNT_PORTRAIT, count);
        editor.apply();
    }

    public static int getPortraitGridSpanCount(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(SPAN_COUNT_PORTRAIT, 2);
    }

    public static void saveLandscapeGridSpanCount(int count) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putInt(SPAN_COUNT_LANDSCAPE, count);
        editor.apply();
    }

    public static int getLandscapeGridSpanCount(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(SPAN_COUNT_LANDSCAPE, 4);
    }

    public static void saveSelectedLightTheme(int id) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putInt(LIGHT_THEME_CATEGORY, id);
        editor.apply();
    }

    public static void saveSelectedDarkTheme(int id) {
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putInt(DARK_THEME_CATEGORY, id);
        editor.apply();
    }

    public static int getSelectedLightTheme(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(LIGHT_THEME_CATEGORY, LIGHT_THEME_1);
    }

    public static int getSelectedDarkTheme(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(DARK_THEME_CATEGORY, DARK_THEME_1);
    }

    @StyleRes
    public static int getThemeToApply() {
        return themeId;
    }

}
