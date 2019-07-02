package com.example.healthcare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.example.healthcare.JavaFile.*;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG="network_receiver";
    private NetworkConnectivityType pre_connectivity=NetworkConnectivityType.NO_NETWORK;
    public static String NETWORK_AVAILABLE="NETWORK_AVAILABLE";
    private LocalBroadcastManager broadcaster;
    public NetworkChangeReceiver(){
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkConnectivityType status = getConnectivityStatus(context);
        Log.e(TAG, "network status"+status.toString());
        broadcaster = LocalBroadcastManager.getInstance(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkConnectivityType.NO_NETWORK) {
                Log.v(TAG,"network not connected");
                pre_connectivity=NetworkConnectivityType.NO_NETWORK;
            } else if (status != pre_connectivity) {
                Log.v(TAG,"there is internet"+intent.getAction());
                if (!pre_connectivity.ifHaveInternet()){
                    broadcaster.sendBroadcast(new Intent(NETWORK_AVAILABLE));
                }
                pre_connectivity=status;
            }
        }
    }
    private NetworkConnectivityType getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return NetworkConnectivityType.WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return NetworkConnectivityType.MOBILE;
        }
        return NetworkConnectivityType.NO_NETWORK;
    }
}
