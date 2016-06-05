package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.avos.avoscloud.AVException;
import com.example.walkarround.Location.activity.LocationActivity;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;

public class ShowLocationActivity extends Activity implements View.OnClickListener {

    private MapView mapView;
    private TextView mTvDetailInfor;
    private TextView mTvFullInfor;
    private ImageView mIvLocateIcon;
    private boolean mBUserSelect = false;
    private AMap aMap;
    private Marker mCurMarker;
    private Marker mTargetMarker;

    AsyncTaskListener mMyPositionListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            GeoData geoData = LocationManager.getInstance(getApplicationContext()).getCurrentLoc();

            if(geoData != null) {
                mCurMarker.setPosition(new LatLng(geoData.getLatitude(), geoData.getLongitude()));
                if(mBUserSelect) {
                    mBUserSelect = false;
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(geoData.getLatitude(), geoData.getLongitude())));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                }
            }
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        initView();
        mapView.onCreate(savedInstanceState);
        initMap();
        LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN, mMyPositionListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    void initView() {
        mapView = (MapView) findViewById(R.id.map);

        //Detail information
        mTvDetailInfor = (TextView)findViewById(R.id.detail);
        mTvFullInfor = (TextView)findViewById(R.id.full_infor);

        //Location icon
        mIvLocateIcon = (ImageView)findViewById(R.id.locate_iv);
        mIvLocateIcon.setOnClickListener(this);
    }

    void initMap() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.msg_select_place_title);

        //Init data
        aMap = mapView.getMap();
        Intent intent = getIntent();
        LatLng latLng = new LatLng(intent.getDoubleExtra(LocationActivity.LATITUDE, 0),
                intent.getDoubleExtra(LocationActivity.LONGITUDE, 0));
        String[] address = intent.getStringExtra(LocationActivity.ADDRESS).split(MessageUtil.MAP_DETAIL_INFOR_SPLIT);
        String addressInfo = null;
        if(address.length > 1) {
            addressInfo = address[1];
        }
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.message_icon_map_position));
        markerOptions.title(address[1]);
        mTargetMarker = aMap.addMarker(markerOptions);
        mTargetMarker.setPosition(latLng);

        MarkerOptions curMarkerOptions = new MarkerOptions();
        curMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_position));
        mCurMarker = aMap.addMarker(curMarkerOptions);

        //Init detail information
        mTvDetailInfor.setText(address[0]);
        mTvFullInfor.setText(addressInfo);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back_rl){
            finish();
        } else if(v.getId() == R.id.locate_iv) {
            mBUserSelect = true;
            LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN, mMyPositionListener);
        }
    }
}
