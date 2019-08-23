package com.hardcodecoder.pulsemusic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    public static int NO_CONNECTION = 146;
    private static int TYPE_WIFI = 1;
    private static int TYPE_MOBILE = 2;
    private static int TYPE_NOT_CONNECTED = 0;
    private static int WIFI_ENABLED = 144;
    private static int MOBILE_ENABLED = 145;

    private static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        int status = -1;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = WIFI_ENABLED/*"Wifi enabled"*/;
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = MOBILE_ENABLED/*"Mobile data enabled"*/;
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = NO_CONNECTION /*"Not connected to Internet"*/;
        }
        return status;
    }
}
