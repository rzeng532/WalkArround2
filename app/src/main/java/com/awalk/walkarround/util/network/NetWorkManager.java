package com.awalk.walkarround.util.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * Created by Richard on 2015/12/26.
 * It is a UTIL manager. Help to get and listen device network modification.
 */
public class NetWorkManager {

    private static NetWorkManager mNetworkManager;
    private NetConnectionReceiver mNetConnectionReceiver;
    private boolean bRigistered = false;

    private NetWorkManager(Context context) {
        bRigistered = false;
        registerNetStatusReceiver(context);
    }

    public static NetWorkManager getInstance(Context context) {
        if(mNetworkManager == null) {
            synchronized (NetWorkManager.class) {
                if(mNetworkManager == null) {
                    mNetworkManager = new NetWorkManager(context);
                }
            }
        }

        return mNetworkManager;
    }

    private void registerNetStatusReceiver(Context context){
        if (!bRigistered){
            mNetConnectionReceiver = new NetConnectionReceiver(context);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.getApplicationContext().registerReceiver(mNetConnectionReceiver,filter);

            //Init network state before network change.
            mNetConnectionReceiver.getNetworkStateByCM(context);
            bRigistered = true;
        }
    }

    private void unRegisterNetStatusReceiver(Context context){
        if (null != context && bRigistered && mNetConnectionReceiver != null){
            context.getApplicationContext().unregisterReceiver(mNetConnectionReceiver);
            mNetConnectionReceiver = null;
            bRigistered = false;
        }
    }

    /*
     * Return TRUE if network is available. Otherwise it will return false.
     */
    public boolean isNetworkAvailable() {
        if(mNetConnectionReceiver == null) {
            return false;
        }

        return (NetConnectionReceiver.NetConnectionStatus.CONNETCTED == mNetConnectionReceiver.getNetConnectionStatus()) ? true : false;
    }

    public void onDestroy(Context context) {
        if(mNetworkManager != null) {
            synchronized (NetWorkManager.class) {
                if(mNetworkManager != null) {
                    unRegisterNetStatusReceiver(context);
                    mNetworkManager = null;
                }
            }
        }
    }
}
