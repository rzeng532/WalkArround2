/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.Location.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.*;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.*;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.walkarround.Location.adapter.LocationAdapter;
import com.example.walkarround.Location.model.LocationItem;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TODO: description
 * Date: 2015-12-16
 *
 * @author Administrator
 */
public class LocationActivity extends Activity implements AMapLocationListener, AMap.OnMapScreenShotListener,
        View.OnClickListener {

    public static final int AROUND_SEARCH_START = 0x10;
    public static final int AROUND_SEARCH_ERROR = 0x11;
    public static final int AROUND_SEARCH_SUC = 0x12;

    public static final int KEY_SEARCH_START = 0x13;
    public static final int KEY_SEARCH_ERROR = 0x14;
    public static final int KEY_SEARCH_SUC = 0x15;

    public static final int KEY_SEARCH = 1;
    public static final int AROUND_SEARCH = 2;

    private static final Logger slogger = Logger.getLogger(LocationActivity.class.getSimpleName());

    private ListView locationListView;
    private AMap aMap;
    private MapView mapView;
    private Marker marker;

    private int formerCheckedIndex = -1;
    private LatLng formerLatLng;

    public static final String ADDRESS = "address";
    public static final String IMAGE_PATH = "imagePath";
    // public static final String MAP_SCREEN_SHOT_URL = "map_screen_shot_url";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    private Button searchButton;
    private ListView searchResultListView;
    private LinearLayout normalLayout;
    private ImageView cancelSearchImage;
    private EditText searchEditText;

    //Map values
    //Map options
    public AMapLocationClientOption mLocationOption = null;
    //Define location listener
    public AMapLocationListener mLocationListener = null;
    public AMapLocationClient mLocationClient = null;

    private LocationAdapter adapter;
    private List<LocationItem> locationItems;
    private List<LocationItem> searchResults;
    private LocationAdapter searchResultAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initView();
        initLocation();
        initMap(savedInstanceState);
        locate();
    }

    @Override
    public void onBackPressed() {
        if (searchResultListView.getVisibility() == View.VISIBLE) {
            searchResultListView.setVisibility(View.GONE);
            normalLayout.setVisibility(View.VISIBLE);
            cancelSearchImage.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void initLocation() {

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
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
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        slogger.e("location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }
                }
            }
        };

        //Set location listener.
        mLocationClient.setLocationListener(mLocationListener);
    }

    private void locate() {
        //从官网拷贝：http://lbs.amap.com/api/android-location-sdk/guide/startlocation/
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private boolean isUserClick = false;

    private void initView() {
        // 返回
        findViewById(R.id.back_tv).setOnClickListener(this);
        // 发送
        findViewById(R.id.location_send_textView).setOnClickListener(this);
        locationListView = (ListView) findViewById(R.id.location_listView);
        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                slogger.d("clicked" + position);
                LocationItem tempLocation = locationItems.get(position);
                locationItems.get(formerCheckedIndex).setChecked(false);
                tempLocation.setChecked(true);
                formerCheckedIndex = position;
                formerLatLng = new LatLng(tempLocation.getLatitude(), tempLocation.getLongitude());
                adapter.notifyDataSetChanged();
                isUserClick = true;
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(tempLocation.getLatitude(), tempLocation
                        .getLongitude())));
            }
        });

        searchEditText = (EditText) findViewById(R.id.location_search_editText);
        searchButton = (Button) findViewById(R.id.search_button);
        searchResultListView = (ListView) findViewById(R.id.search_result_listView);
        normalLayout = (LinearLayout) findViewById(R.id.normal_layout);
        cancelSearchImage = (ImageView) findViewById(R.id.cancel_location_search_imageView);

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String temp = s.toString();
                if (temp == null || temp.isEmpty()) {
                    searchButton.setEnabled(false);
                    searchResultListView.setVisibility(View.GONE);
                    normalLayout.setVisibility(View.VISIBLE);
                    cancelSearchImage.setVisibility(View.INVISIBLE);
                } else {
                    searchButton.setEnabled(true);
                    cancelSearchImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchButton.setOnClickListener(this);
        cancelSearchImage.setOnClickListener(this);

        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocationItem tempLocation = searchResults.get(position);
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(tempLocation.getLatitude(), tempLocation
                        .getLongitude())));
                searchEditText.setText("");
            }
        });

        locationItems = new ArrayList<LocationItem>();
        adapter = new LocationAdapter(LocationActivity.this, locationItems, AROUND_SEARCH);
        locationListView.setAdapter(adapter);

        searchResults = new ArrayList<LocationItem>();
        searchResultAdapter = new LocationAdapter(LocationActivity.this, searchResults, KEY_SEARCH);
        searchResultListView.setAdapter(searchResultAdapter);
    }

    private void initMap(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.message_icon_map_position));
        marker = aMap.addMarker(markerOptions);
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {

            @Override
            public void onCameraChangeFinish(CameraPosition arg0) {
                slogger.d("onCameraChangeFinish");
                slogger.d("isUserClick:" + isUserClick);
                if (isUserClick) {
                    isUserClick = false;
                    marker.setPosition(new LatLng(arg0.target.latitude, arg0.target.longitude));
                    return;
                } else {
                    if (!arg0.target.equals(formerLatLng)) {

                        locationItems.clear();

                        regeocoder(arg0.target, AROUND_SEARCH, "");

                        formerLatLng = arg0.target;

                    }
                }
            }

            @Override
            public void onCameraChange(CameraPosition arg0) {
                slogger.d("onCameraChange");
                if (!isUserClick) {
                    marker.setPosition(new LatLng(arg0.target.latitude, arg0.target.longitude));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mLocationClient.stopLocation();
        // deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.onDestroy();
        hideDialog();
    }

    // Implements AMapLocationListener
    @Override
    public void onLocationChanged(AMapLocation arg0) {
        if (arg0 != null && arg0.getErrorCode() == 0) {
            Bundle locBundle = arg0.getExtras();
            if (locBundle != null) {
                locBundle.getString("desc");
            }
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude())));
            aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    // Implements OnMapScreenShotListener
    @Override
    public void onMapScreenShot(Bitmap arg0) {
        slogger.d("original:" + arg0.getByteCount());
        try {
            String filePath = null;
            //We don't need map screenshot now. We will open it later.
            //if (AppConstant.IS_ENABLE_MSG_LOCATION_PIC) {
            //    filePath = createLocationPic(arg0);
            //}
            LocationItem temp = locationItems.get(formerCheckedIndex);
            Intent resultIntent = new Intent();
            Bundle dataBundle = new Bundle();
            dataBundle.putString(ADDRESS, temp.getSubtitle());
            //dataBundle.putString(IMAGE_PATH, filePath);
            // dataBundle.putString(MAP_SCREEN_SHOT_URL, mapScreenShotUrl);
            dataBundle.putDouble(LATITUDE, temp.getLatitude());
            dataBundle.putDouble(LONGITUDE, temp.getLongitude());
            resultIntent.putExtras(dataBundle);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            // handle exception
            slogger.e("onMapScreenShot Exception :" + e.getMessage());
        }
    }

    private String createLocationPic(Bitmap arg0) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        arg0.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        Bitmap smallBitmap = getBitmapByBytes(outputStream.toByteArray(), 540);
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        slogger.d("after:" + smallBitmap.getByteCount());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filePath = WalkArroundApp.MTC_DATA_PATH + AppConstant.LOCATION_PIC_PATH;
        File folder = new File(filePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        filePath += (sdf.format(new Date()) + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            boolean b = smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    /**
     * 压缩图片
     *
     * @param bytes
     * @return
     */
    public static Bitmap getBitmapByBytes(byte[] bytes, int maxSize) {

        // 对于图片的二次采样,主要得到图片的宽与高
        int width = 0;
        int height = 0;
        int sampleSize = 1; // 默认缩放为1
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 仅仅解码边缘区域
        // 如果指定了inJustDecodeBounds，decodeByteArray将返回为空
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        // 得到宽与高
        height = options.outHeight;
        width = options.outWidth;

        // 图片实际的宽与高，根据默认最大值，得到图片实际的缩放比例
        while ((height / sampleSize > maxSize) || (width / sampleSize > maxSize)) {
            sampleSize *= 2;
        }

        // 不再只加载图片实际边缘
        options.inJustDecodeBounds = false;
        // 并且制定缩放比例
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    Handler uiHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case AROUND_SEARCH_START:
                    showDialog();
                    break;

                case AROUND_SEARCH_ERROR:
                    hideDialog();
                    Toast.makeText(LocationActivity.this, "搜索失败", Toast.LENGTH_SHORT).show();
                    break;
                case AROUND_SEARCH_SUC:
                    hideDialog();
                    adapter.notifyDataSetChanged();
                    break;
                case KEY_SEARCH_START:
                    showDialog();
                    break;
                case KEY_SEARCH_ERROR:
                    hideDialog();
                    Toast.makeText(LocationActivity.this, "搜索失败", Toast.LENGTH_SHORT).show();
                    break;
                case KEY_SEARCH_SUC:
                    hideDialog();
                    searchResultAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    /**
     * 周边搜索
     *
     * @param lat      中心点经度
     * @param lng      中心点维度
     * @param key      搜索关键字
     * @param type     搜素类型/地名
     * @param cityCode 城市编码
     */
    private void newSearch(double lat, double lng, String key, String type, String cityCode, final int searchType) {
        PoiSearch.Query query = new PoiSearch.Query(key, type, cityCode);
        query.setPageSize(20);
        query.setPageNum(0);
        PoiSearch poiSearch = new PoiSearch(this, query);
        if (searchType == AROUND_SEARCH) {
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(lat, lng), 1000));
        }

        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {

            @Override
            public void onPoiSearched(PoiResult arg0, int arg1) {
                slogger.d("search ok");
                if (arg0 == null || arg0.getPois() == null) {
                    if (searchType == AROUND_SEARCH) {
                        uiHandler.sendEmptyMessage(AROUND_SEARCH_ERROR);
                    } else if (searchType == KEY_SEARCH) {
                        uiHandler.sendEmptyMessage(KEY_SEARCH_ERROR);
                    }
                    return;
                } else {
                    for (PoiItem item : arg0.getPois()) {
                        slogger.d("snippet:" + item.getSnippet() + " title:" + item.getTitle() + "\n");
                        LocationItem tempLocation = new LocationItem();
                        tempLocation.setChecked(false);
                        tempLocation.setLatitude(item.getLatLonPoint().getLatitude());
                        tempLocation.setLongitude(item.getLatLonPoint().getLongitude());
                        String title = item.getTitle();
                        tempLocation.setTitle(title);
                        String subtitle = item.getSnippet();
                        tempLocation.setSubtitle(subtitle == null || subtitle.isEmpty() ? title : subtitle);

                        if (searchType == AROUND_SEARCH) {
                            locationItems.add(tempLocation);
                        } else if (searchType == KEY_SEARCH) {
                            searchResults.add(tempLocation);
                        }
                    }
                    if (searchType == AROUND_SEARCH) {
                        uiHandler.sendEmptyMessage(AROUND_SEARCH_SUC);
                    } else if (searchType == KEY_SEARCH) {
                        uiHandler.sendEmptyMessage(KEY_SEARCH_SUC);
                    }
                }

            }
        });
        poiSearch.searchPOIAsyn();
    }

    /**
     * 根据经纬度逆编码地理位置
     *
     * @param latLng
     */
    private void regeocoder(final LatLng latLng, final int searchType, final String key) {

        if (searchType == AROUND_SEARCH) {
            uiHandler.sendEmptyMessage(AROUND_SEARCH_START);
        } else if (searchType == KEY_SEARCH) {
            uiHandler.sendEmptyMessage(KEY_SEARCH_START);
        }

        GeocodeSearch search = new GeocodeSearch(this);
        search.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {

            @Override
            public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
                if (arg1 == 0 && arg0 != null) {
                    RegeocodeAddress regeocodeAddress = arg0.getRegeocodeAddress();
                    if (searchType == AROUND_SEARCH) {
                        LocationItem item = new LocationItem();
                        item.setChecked(true);
                        formerCheckedIndex = 0;
                        item.setLatitude(latLng.latitude);
                        item.setLongitude(latLng.longitude);
                        item.setTitle("[位置]");
                        item.setSubtitle(regeocodeAddress.getFormatAddress());
                        locationItems.add(item);
                    }
                    newSearch(latLng.latitude, latLng.longitude, key, searchType == AROUND_SEARCH ? "" : "",
                            regeocodeAddress.getCityCode(), searchType);
                } else {
                    if (searchType == AROUND_SEARCH) {
                        uiHandler.sendEmptyMessage(AROUND_SEARCH_ERROR);
                    } else if (searchType == KEY_SEARCH) {
                        uiHandler.sendEmptyMessage(KEY_SEARCH_ERROR);
                    }
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

            }
        });
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude, latLng.longitude), 200,
                GeocodeSearch.AMAP);
        search.getFromLocationAsyn(query);
    }

    private Dialog loadDialog;

    private void showDialog() {
        if (loadDialog == null) {
            loadDialog = DialogFactory.getLoadingDialog(this, "加载中...", false, null);
        }
        if (!loadDialog.isShowing()) {
            loadDialog.show();
        }
    }

    private void hideDialog() {
        if (loadDialog != null) {
            loadDialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.location_send_textView:
                aMap.getMapScreenShot(LocationActivity.this);
                break;
            case R.id.back_tv:
                finish();
                break;
            case R.id.cancel_location_search_imageView:
                searchEditText.setText("");
                break;
            case R.id.search_button:
                normalLayout.setVisibility(View.GONE);
                searchResultListView.setVisibility(View.VISIBLE);
                searchResults.clear();
                regeocoder(formerLatLng, KEY_SEARCH, searchEditText.getText().toString());
            default:
                break;
        }
    }
}
