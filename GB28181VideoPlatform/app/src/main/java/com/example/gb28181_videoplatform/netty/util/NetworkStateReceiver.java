package com.example.gb28181_videoplatform.netty.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkStateReceiver extends BroadcastReceiver {
    Logger mLog= LoggerFactory.getLogger(NetworkStateReceiver.class);

    public NetworkStateReceiver() {

    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    PoliceService.instance.startWork();
                }
            }
            mLog.debug("network state change,power on and start");
        }
    }
}
