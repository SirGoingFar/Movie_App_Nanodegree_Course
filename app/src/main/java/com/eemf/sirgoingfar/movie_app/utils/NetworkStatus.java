package com.eemf.sirgoingfar.movie_app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatus {

    public static boolean isConnected(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm == null)
            return false;

        NetworkInfo info = cm.getActiveNetworkInfo();

        return (info != null
                && (info.isConnected() || info.isConnectedOrConnecting()));
    }

    public static boolean isPoorConectivity(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm == null)
            return true;

        NetworkInfo info = cm.getActiveNetworkInfo();

        NetworkInfo.DetailedState detailedState;

        if(info != null)
            detailedState = info.getDetailedState();
        else
            return true;

        return (detailedState == NetworkInfo.DetailedState.DISCONNECTED
                || detailedState == NetworkInfo.DetailedState.DISCONNECTING
                || detailedState == NetworkInfo.DetailedState.SUSPENDED
                || detailedState == NetworkInfo.DetailedState.VERIFYING_POOR_LINK
                || detailedState == NetworkInfo.DetailedState.FAILED
                || detailedState == NetworkInfo.DetailedState.BLOCKED);
    }
}
