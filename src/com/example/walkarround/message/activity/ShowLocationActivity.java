package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;

public class ShowLocationActivity extends Activity implements View.OnClickListener {

    private MapView mapView;
    private AMap aMap;
    private Marker mCurMarker;
    private Marker mTargetMarker;

    private TextView mTvSelectPosition;
    private TextView mTvAcceptPosition;

    AsyncTaskListener mMyPositionListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            GeoData geoData = LocationManager.getInstance(getApplicationContext()).getCurrentLoc();

            if(geoData != null) {
                mCurMarker.setPosition(new LatLng(geoData.getLatitude(), geoData.getLongitude()));
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
        LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_SHOW_LOCATION, mMyPositionListener);
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

        int sender = getIntent().getIntExtra(LocationActivity.SENDER_OR_RECEIVER, 0);
        if(sender == MessageConstant.MessageSendReceive.MSG_RECEIVE) {
            findViewById(R.id.bottom_layout).setVisibility(View.VISIBLE);
            mTvSelectPosition = (TextView)findViewById(R.id.select_another);
            mTvSelectPosition.setOnClickListener(this);
            mTvAcceptPosition = (TextView)findViewById(R.id.accept_place);
            mTvAcceptPosition.setOnClickListener(this);
        } else {
            findViewById(R.id.bottom_layout).setVisibility(View.GONE);
        }
    }

    void initMap() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.msg_select_place_title);

        aMap = mapView.getMap();
        Intent intent = getIntent();
        LatLng latLng = new LatLng(intent.getDoubleExtra(LocationActivity.LATITUDE, 0),
                intent.getDoubleExtra(LocationActivity.LONGITUDE, 0));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        MarkerOptions markerOptions = new MarkerOptions();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.message_icon_map_position));
        markerOptions.title(intent.getStringExtra(LocationActivity.ADDRESS));
        mTargetMarker = aMap.addMarker(markerOptions);
        mTargetMarker.setPosition(latLng);

        MarkerOptions curMarkerOptions = new MarkerOptions();
        curMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_position));
        mCurMarker = aMap.addMarker(curMarkerOptions);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.select_another:
                //Select another place
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.accept_place:
                //Accept current place
                break;
            default:
                break;
        }
    }
}
