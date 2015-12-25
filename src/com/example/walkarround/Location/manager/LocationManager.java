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
    public AMapLocationClientOption mLocOption = null;
    //Define location listener
    public AMapLocationListener mLocListener = null;
    public AMapLocationClient mLocClient = null;

    /*
     * Save listener by KEY.
     * If user invoke method twice by same key, last one will be saved.
     */
    private HashMap<String, AsyncTaskListener> mListenerMap = new HashMap<>();

    /*
     * context should application context.
     */
    private LocationManager(Context context) {
        mLocClient = new AMapLocationClient(context);
        mLocListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    AsyncTaskListener tmpListener = null;

                    if (amapLocation.getErrorCode() == 0) {
                    /*
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    amapLocation.getLatitude();//获取经度
                    amapLocation.getLongitude();//获取纬度
                    amapLocation.getAccuracy();//获取精度信息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(amapLocation.getTime());
                    df.format(date);//定位时间
                    amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果
                    amapLocation.getCountry();//国家信息
                    amapLocation.getProvince();//省信息
                    amapLocation.getCity();//城市信息
                    amapLocation.getDistrict();//城区信息
                    amapLocation.getRoad();//街道信息
                    amapLocation.getCityCode();//城市编码
                    amapLocation.getAdCode();//地区编码
                    */
                        mCurGeo = new GeoData(amapLocation.getLatitude(), amapLocation.getLongitude(), amapLocation.getCity() + amapLocation.getDistrict());
                        triggerAllListener(true, null);
                    } else {
                        AVException e = new AVIMException(amapLocation.getErrorCode(), amapLocation.getErrorInfo());
                        triggerAllListener(false, e);
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        lmLogger.e("location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }

                    removeAllLocListener();
                }
            }
        };
        mLocClient.setLocationListener(mLocListener);

        locate();
    }

    private void locate() {
        //从官网拷贝：http://lbs.amap.com/api/android-location-sdk/guide/startlocation/
        //初始化定位参数
        mLocOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocOption.setOnceLocation(true); //FALSE will repeat to do.
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocOption.setInterval(20000); //20s
        //给定位客户端对象设置定位参数
        mLocClient.setLocationOption(mLocOption);
        //启动定位
        mLocClient.startLocation();
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
        mLocClient.onDestroy();
        mLocClient = null;
        mLocListener = null;
        mLocClient = null;
        mLocationManager = null;
    }

    /*
     * Return current position which get from last locate.
     */
    public GeoData getCurrentLoc() {
        return mCurGeo;
    }

    /*
     * Locate again and call listener onSuccess if we get position from SDK.
     */
    public void locateCurPosition(String key, AsyncTaskListener listener) {
        addLocListener(key, listener);
        locate();
    }

    /*
     * AsyncTaskListener hash map operations.
     * Include invoking onSuccess, onFailed, add listener, remove listener.
     */
    private void addLocListener(String key, AsyncTaskListener listener) {
        if(TextUtils.isEmpty(key) || listener == null) {
            return;
        }

        if(mListenerMap == null) {
            mListenerMap = new HashMap<>();
        }

        synchronized (LocationManager.class) {
            mListenerMap.put(key, listener);
        }
    }

    private void removeLocListener(String key) {
        if(TextUtils.isEmpty(key) || mListenerMap == null) {
            return;
        }

        synchronized (LocationManager.class) {
            /*
             * We just set listener as NULL here.
             */
            mListenerMap.put(key, null);
        }
    }

    /*
     * If bSuccess == true, e should can be null. Otherewise, it should be an exception.
     */
    private void triggerLocListener(Map.Entry<String, AsyncTaskListener> entry, boolean bSuccess, AVException e) {
            if(bSuccess) {
                entry.getValue().onSuccess();
            } else {
                entry.getValue().onFailed(e);
            }
    }

    private void triggerAllListener(boolean bSuccess, AVException e) {
        synchronized (LocationManager.class) {
            for(Map.Entry<String, AsyncTaskListener> entry : mListenerMap.entrySet()){
                triggerLocListener(entry, bSuccess, e);
            }
        }
    }

    private void removeAllLocListener() {
        for(String key : mListenerMap.keySet()) {
            removeLocListener(key);
        }
    }
}
