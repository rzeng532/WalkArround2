/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.Location.model;

import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVObject;
import com.example.walkarround.myself.util.ProfileUtil;

/**
 * TODO: description
 * Date: 2015-12-17
 *
 * @author Richard
 */
public class GeoData {

    private double mLatitude;
    private double mLongitude;
    private String mAddrInfor;

    public GeoData(double latitude, double longitude, String addr) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mAddrInfor = addr;
    }

    public GeoData(AVObject obj) {
        if(obj == null) {
            return;
        }

        AVGeoPoint geoPoint = (AVGeoPoint)obj.get(ProfileUtil.REG_KEY_LOCATION);
        if(geoPoint != null) {
            this.mLatitude = geoPoint.getLatitude();
            this.mLongitude = geoPoint.getLongitude();
        }

        this.mAddrInfor = (String)obj.get(ProfileUtil.REG_KEY_LOCATION_ADDR);
    }


    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getAddrInfor() {
        return mAddrInfor;
    }

    public void setAddrInfor(String mAddrInfor) {
        this.mAddrInfor = mAddrInfor;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
