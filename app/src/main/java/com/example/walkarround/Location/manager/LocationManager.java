package com.example.walkarround.Location.manager;

import android.content.Context;
import android.text.TextUtils;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMException;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Richard on 2015/12/22.
 */
public class LocationManager {
    private static final Logger lmLogger = Logger.getLogger(LocationManager.class.getSimpleName());
    private static LocationManager mLocationManager;

    private GeoData mCurGeo = null;

    //Map options
    public AMapLocationClientOption mLocOptionSingle = null;
    public AMapLocationClientOption mLocOptionContinue = null;

    //Define location listener
    public AMapLocationListener mLocListenerSingle = null;
    public AMapLocationClient mLocClientSingle = null;
    private byte[] mContinueLocLock = new byte[0];

    public AMapLocationListener mLocListenerContinue = null;
    public AMapLocationClient mLocClientContinue = null;

    /*
     * Save listener by KEY.
     * If user invoke method twice by same key, last one will be saved.
     */
    private HashMap<String, AsyncTaskListener> mSingleLocateListenerMap = new HashMap<>();
    private HashMap<String, AsyncTaskListener> mContinueLocateListenerMap = new HashMap<>();

    /*
     * context should application context.
     */
    private LocationManager(Context context) {
        //Init single locate client.
        mLocClientSingle = new AMapLocationClient(context);
        mLocListenerSingle = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    AsyncTaskListener tmpListener = null;

                    if (amapLocation.getErrorCode() == 0) {
                        updateMyCurLocation(amapLocation);
                        triggerAllSingleLocListener(true, null);
                    } else {
                        AVException e = new AVIMException(amapLocation.getErrorCode(), amapLocation.getErrorInfo());
                        triggerAllSingleLocListener(false, e);
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        lmLogger.e("location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }

                    removeAllSingleLocListener();
                }
            }
        };
        mLocClientSingle.setLocationListener(mLocListenerSingle);

        locateSingle();
    }

    private void locateSingle() {
        //从官网拷贝：http://lbs.amap.com/api/android-location-sdk/guide/startlocation/
        //初始化定位参数
        mLocOptionSingle = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocOptionSingle.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocOptionSingle.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocOptionSingle.setOnceLocation(true); //FALSE will repeat to do.
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocOptionSingle.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocOptionSingle.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        //mLocOptionSingle.setInterval(20000); //20s
        //给定位客户端对象设置定位参数
        mLocClientSingle.setLocationOption(mLocOptionSingle);
        //启动定位
        mLocClientSingle.startLocation();
    }

    public void start2ContinueLocate(String key, AsyncTaskListener listener) {

        synchronized (mContinueLocLock) {
            addContinueLocListener(key,listener);
        }

        if (mLocClientContinue == null) {
            synchronized (mContinueLocLock) {
                if (mLocClientContinue == null) {
                    //Init continue locate client.
                    mLocClientContinue = new AMapLocationClient(WalkArroundApp.getInstance());
                    mLocListenerContinue = new AMapLocationListener() {
                        @Override
                        public void onLocationChanged(AMapLocation amapLocation) {
                            if (amapLocation != null) {
                                AsyncTaskListener tmpListener = null;

                                if (amapLocation.getErrorCode() == 0) {
                                    updateMyCurLocation(amapLocation);
                                    triggerAllContinueLocListener(true, null);
                                } else {
                                    AVException e = new AVIMException(amapLocation.getErrorCode(), amapLocation.getErrorInfo());
                                    triggerAllContinueLocListener(false, e);
                                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                                    lmLogger.e("location Error, ErrCode:"
                                            + amapLocation.getErrorCode() + ", errInfo:"
                                            + amapLocation.getErrorInfo());
                                }
                            }
                        }
                    };
                    mLocClientContinue.setLocationListener(mLocListenerContinue);
                }
            }
        }

        if(!mLocClientContinue.isStarted()) {
            locateContinue(AppConstant.MAP_CONTINUE_LOC_INTERVAL); //Set 3s as inverval time
        }
    }

    private synchronized void locateContinue(long repeatTime) {
        //从官网拷贝：http://lbs.amap.com/api/android-location-sdk/guide/startlocation/
        //初始化定位参数
        mLocOptionContinue = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocOptionContinue.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocOptionContinue.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocOptionContinue.setOnceLocation(false); //FALSE will repeat to do.
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocOptionContinue.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocOptionContinue.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocOptionContinue.setInterval(repeatTime <= 1000 ? 1000 : repeatTime); //1 second at least
        //给定位客户端对象设置定位参数
        mLocClientContinue.setLocationOption(mLocOptionContinue);
        //启动定位
        mLocClientContinue.startLocation();
    }

    public void stopContinueLocate() {
        synchronized (mContinueLocLock) {
            if (mLocClientContinue != null) {
                mLocClientContinue.stopLocation();
                removeAllContinueLocListener();
            }
        }
    }

    public static LocationManager getInstance(Context context) {
        if (mLocationManager == null) {
            synchronized (LocationManager.class) {
                if (mLocationManager == null) {
                    mLocationManager = new LocationManager(context);
                }
            }
        }

        return mLocationManager;
    }

    public void onDestroy() {
        if (mLocClientSingle != null) {
            mLocClientSingle.onDestroy();
            mLocClientSingle = null;
        }
        mLocListenerSingle = null;

        if (mLocClientContinue != null) {
            stopContinueLocate();
            mLocClientContinue.onDestroy();
            mLocClientContinue = null;
        }
        mLocListenerContinue = null;

        mLocationManager = null;
    }

    /*
     * Return current position which get from last locateSingle.
     */
    public GeoData getCurrentLoc() {
        return mCurGeo;
    }

    /*
     * Locate again and call listener onSuccess if we get position from SDK.
     */
    public void locateCurPosition(String key, AsyncTaskListener listener) {
        addSingleLocListener(key, listener);
        locateSingle();
    }

    /*
     * AsyncTaskListener hash map operations.
     * Include invoking onSuccess, onFailed, add listener, remove listener.
     */
    private synchronized void addSingleLocListener(String key, AsyncTaskListener listener) {
        if (TextUtils.isEmpty(key) || listener == null) {
            return;
        }

        if (mSingleLocateListenerMap == null) {
            mSingleLocateListenerMap = new HashMap<>();
        }

        mSingleLocateListenerMap.put(key, listener);
    }

    /*
     * If bSuccess == true, e should can be null. Otherewise, it should be an exception.
     */
    private void triggerLocListener(Map.Entry<String, AsyncTaskListener> entry, boolean bSuccess, AVException e) {
        if (bSuccess) {
            entry.getValue().onSuccess(null);
        } else {
            entry.getValue().onFailed(e);
        }
    }

    private synchronized void triggerAllSingleLocListener(boolean bSuccess, AVException e) {
        for (Map.Entry<String, AsyncTaskListener> entry : mSingleLocateListenerMap.entrySet()) {
            triggerLocListener(entry, bSuccess, e);
        }
    }

    private synchronized void removeAllSingleLocListener() {
        if (mSingleLocateListenerMap != null) {
            mSingleLocateListenerMap.clear();
        }
    }

    private void triggerAllContinueLocListener(boolean bSuccess, AVException e) {
        synchronized(mContinueLocLock) {
            for (Map.Entry<String, AsyncTaskListener> entry : mContinueLocateListenerMap.entrySet()) {
                triggerLocListener(entry, bSuccess, e);
            }
        }
    }


    private void addContinueLocListener(String key, AsyncTaskListener listener) {
        if (TextUtils.isEmpty(key) || listener == null) {
            return;
        }

        synchronized (mContinueLocLock) {
            if (mContinueLocateListenerMap == null) {
                mContinueLocateListenerMap = new HashMap<>();
            }

            mContinueLocateListenerMap.put(key, listener);
        }
    }

    private void removeAllContinueLocListener() {
        synchronized (mContinueLocLock) {
            if (mContinueLocateListenerMap != null) {
                mContinueLocateListenerMap.clear();
            }
        }
    }

    private synchronized void updateMyCurLocation(AMapLocation amapLocation) {
        mCurGeo = new GeoData(amapLocation.getLatitude(), amapLocation.getLongitude(), amapLocation.getCity() + amapLocation.getDistrict());
    }
}
