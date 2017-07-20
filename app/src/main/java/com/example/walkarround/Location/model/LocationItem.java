/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.Location.model;

import java.io.Serializable;

/**
 * TODO: description
 * Date: 2015-12-16
 *
 * @author Richard
 */
public class LocationItem implements Serializable {

    private static final long serialVersionUID = -992283504306834922L;

    private String title;
    private String subtitle;
    private double latitude;
    private double longitude;
    private boolean checked;
    private String locationPicUrl;

    public LocationItem() {
    }

    public LocationItem(String title, String subtitle, double lat, double lng, boolean checked) {
        this.title = title;
        this.subtitle = subtitle;
        this.latitude = lat;
        this.longitude = lng;
        this.checked = checked;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLongitude(double lng) {
        this.longitude = lng;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean getChecked() {
        return this.checked;
    }

    public String getLocationPicUrl() {
        return locationPicUrl;
    }

    public void setLocationPicUrl(String locationPicUrl) {
        this.locationPicUrl = locationPicUrl;
    }
}

