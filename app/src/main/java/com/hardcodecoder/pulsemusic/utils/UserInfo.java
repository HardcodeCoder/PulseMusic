package com.hardcodecoder.pulsemusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo {

    private static final String NAME_KEY = "UserName";
    private static final String AVATAR_KEY = "AvatarUri";


    public static void saveUserName(Context context, String name) {
        SharedPreferences.Editor editor = context.getSharedPreferences(NAME_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(NAME_KEY, name);
        editor.apply();
    }

    public static String getUserName(Context context) {
        return  context.getSharedPreferences(NAME_KEY, Context.MODE_PRIVATE).getString(NAME_KEY, "User");
    }

    public static void saveUserProfilePic(Context context, String path) {
        SharedPreferences.Editor editor = context.getSharedPreferences(AVATAR_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(AVATAR_KEY, path);
        editor.apply();
    }

    public static String getUserProfilePic(Context context) {
        return context.getSharedPreferences(AVATAR_KEY, Context.MODE_PRIVATE).getString(AVATAR_KEY, "");
    }

}
