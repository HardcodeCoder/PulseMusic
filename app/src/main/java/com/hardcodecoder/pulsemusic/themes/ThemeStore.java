package com.hardcodecoder.pulsemusic.themes;

import androidx.annotation.StyleRes;

import com.hardcodecoder.pulsemusic.R;

public class ThemeStore {

    //public static final String LIGHT_THEME_CATEGORY = "Light_themes_key";
    public static final String DARK_THEME_CATEGORY = "Dark_themes_key";
    public static final String ACCENT_COLOR = "Accent_Color";

    static final short LIGHT_THEME_1 = 515;
    /*public static final short LIGHT_THEME_2 = 525;
    public static final short LIGHT_THEME_3 = 535;*/

    public static final short DARK_THEME_1 = 616;
    public static final short DARK_THEME_2 = 626;
    public static final short DARK_THEME_3 = 636;

    public static final int CINNAMON = 10;
    public static final int GREEN = 11;
    public static final int OCEAN = 12;
    public static final int ORCHID = 13;
    public static final int BLUE = 14;
    public static final int PURPLE = 15;
    public static final int SPACE = 16;


    @StyleRes
    static int getThemeById(boolean darkModeOn, int id) {
        switch (id) {
            /*case LIGHT_THEME_1:
                return R.style.ActivityThemeLight;
            case LIGHT_THEME_2:
                return R.style.ActivityThemeBlackWhite;
            case LIGHT_THEME_3:
                return R.style.ActivityThemeSweetMorning;*/
            case DARK_THEME_1:
                return R.style.ActivityThemeDark;
            case DARK_THEME_2:
                return R.style.ActivityThemeKindaDark;
            case DARK_THEME_3:
                return R.style.ActivityThemeBlack;
        }
        return darkModeOn ? R.style.ActivityThemeDark : R.style.ActivityThemeLight;
    }

    @StyleRes
    static int getAccentById(boolean darkModeOn, int id) {
        if (darkModeOn) {
            switch (id) {
                case CINNAMON:
                    return R.style.CinnamonDark;
                case GREEN:
                    return R.style.GreenDark;
                case OCEAN:
                    return R.style.OceanDark;
                case ORCHID:
                    return R.style.OrchidDark;
                case BLUE:
                    return R.style.BlueDark;
                case SPACE:
                    return R.style.SpaceDark;
                case PURPLE:
                default:
                    return R.style.PurpleDark;
            }
        } else {
            switch (id) {
                case CINNAMON:
                    return R.style.Cinnamon;
                case GREEN:
                    return R.style.Green;
                case OCEAN:
                    return R.style.Ocean;
                case ORCHID:
                    return R.style.Orchid;
                case BLUE:
                    return R.style.Blue;
                case SPACE:
                    return R.style.Space;
                case PURPLE:
                default:
                    return R.style.Purple;
            }
        }
    }
}
