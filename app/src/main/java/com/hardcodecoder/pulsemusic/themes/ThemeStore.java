package com.hardcodecoder.pulsemusic.themes;

import androidx.annotation.StyleRes;

import com.hardcodecoder.pulsemusic.R;

public class ThemeStore {

    public static final String LIGHT_THEME_CATEGORY = "light_themes_key";
    public static final String DARK_THEME_CATEGORY = "dark_themes_key";
    public static final short LIGHT_THEME_1 = 515;
    public static final short LIGHT_THEME_2 = 525;
    public static final short LIGHT_THEME_3 = 535;
    public static final short DARK_THEME_1 = 616;
    public static final short DARK_THEME_2 = 626;
    public static final short DARK_THEME_3 = 636;

    @StyleRes
    static int getThemeById(boolean darkModeOn, int id) {
        switch (id) {
            case LIGHT_THEME_1: return R.style.ActivityThemeLight;
            case LIGHT_THEME_2: return R.style.ActivityThemeBlackWhite;
            case LIGHT_THEME_3: return R.style.ActivityThemeSweetMorning;
            case DARK_THEME_1: return R.style.ActivityThemeDark;
            case DARK_THEME_2: return R.style.ActivityThemeKindaDark;
            case DARK_THEME_3: return R.style.ActivityThemeCharcoal;
        }
        return darkModeOn ? R.style.ActivityThemeDark : R.style.ActivityThemeLight;
    }
}
