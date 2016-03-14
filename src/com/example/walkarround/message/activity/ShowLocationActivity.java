package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.example.walkarround.Location.activity.LocationActivity;
import com.example.walkarround.R;

public class ShowLocationActivity extends Activity {

    private MapView mapView;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        initView();
        mapView.onCreate(savedInstanceState);
        initMap();
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
    }

    void initMap() {
        aMap = mapView.getMap();
        Intent intent = getIntent();
        LatLng latLng = new LatLng(intent.getDoubleExtra(LocationActivity.LATITUDE, 0),
                intent.getDoubleExtra(LocationActivity.LONGITUDE, 0));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.message_icon_map_position));
        markerOptions.title(intent.getStringExtra(LocationActivity.ADDRESS));
        aMap.addMarker(markerOptions);
    }

    public void showLocationBack(View view) {
        this.finish();
    }
}
