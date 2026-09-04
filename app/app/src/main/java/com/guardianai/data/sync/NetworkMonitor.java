package com.guardianai.data.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

public class NetworkMonitor {

    private static final String TAG = "NetworkMonitor";
    private static NetworkMonitor instance;

    private final Context context;
    private final ConnectivityManager connectivityManager;
    private boolean isOnline = false;

    private NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        checkInitialState();
        registerCallback();
    }

    public static synchronized NetworkMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkMonitor(context);
        }
        return instance;
    }

    public boolean isOnline() {
        return isOnline;
    }

    private void checkInitialState() {
        if (connectivityManager == null) {
            isOnline = false;
            return;
        }
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            isOnline = false;
            return;
        }
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
        isOnline = caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    private void registerCallback() {
        if (connectivityManager == null) return;
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        try {
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Log.i(TAG, "Network connection restored (ONLINE).");
                    boolean wasOffline = !isOnline;
                    isOnline = true;
                    if (wasOffline) {
                        SyncManager.getInstance(context).triggerSync();
                    }
                }

                @Override
                public void onLost(Network network) {
                    Log.w(TAG, "Network connection lost (OFFLINE).");
                    isOnline = false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error registering NetworkCallback: " + e.getMessage());
        }
    }
}
