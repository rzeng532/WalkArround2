package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;

/**
 * 好友位置信息
 * Date: 2018-06-14
 *
 * @author cmcc
 */
public class UserCoordinate {

    private LocationInfo result;

    public LocationInfo getResult() {
        return result;
    }

    public void setResult(LocationInfo result) {
        this.result = result;
    }

    public class LocationInfo implements Serializable {

        private double latitude;
        private double longitude;


        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
