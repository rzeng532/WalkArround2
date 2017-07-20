package com.example.walkarround.util.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.walkarround.util.Logger;

/**
 * Created by Richard on 2015/12/26.
 */
public class NetConnectionReceiver extends BroadcastReceiver {
    private Logger logger = Logger.getLogger(NetConnectionReceiver.class.getSimpleName());

    private static NetConnectionStatus sNetStatus = NetConnectionStatus.CONNETCTED;//default
    public NetConnectionReceiver(Context context){
        getNetWorkInfo(context);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        logger.i(action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            getNetWorkInfo(context);
        }
    }

    private void getNetWorkInfo(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();

        if (activeInfo == null) {
            sNetStatus = NetConnectionStatus.DISCONNETED;
        } else {
            sNetStatus = NetConnectionStatus.CONNETCTED;
        }
    }

    protected static NetConnectionStatus getNetConnectionStatus(){
        return sNetStatus;
    }

    protected boolean getNetworkStateByCM(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        sNetStatus = NetConnectionStatus.DISCONNETED;
        if(null == connMgr) {
            return false;
        }

        NetworkInfo activeNet = connMgr.getActiveNetworkInfo();
        if(null != activeNet && activeNet.isAvailable()) {
            sNetStatus = NetConnectionStatus.CONNETCTED;
            return true;
        }

        return false;
    }

    public enum NetConnectionStatus {
        CONNETCTED,
        DISCONNETED
    }
}
