package com.example.walkarround.myself.model;

import com.example.walkarround.Location.model.GeoData;

/**
 * Created by Richard on 2015/12/7.
 * This class defined what will be saved for my profile information
 * (Except user name, phone. Those information saved on AVUser).
 */
public class MyProfileInfo {
    String mUsrName = null; //user name
    String mPortraitPath = null; //portrait file path
    String mBirthday = null;
    String mSignature = null;
    String mMobileNum = null;
    String mGendle = null; //0: men, 1: female
    GeoData mLocation = null;
    private String mSpeedDateId;
    private String mDynamicDataId;
    private int mUserDateState = -1;

    public String getMobileNum() {
        return mMobileNum;
    }

    public void setMobileNum(String mobile) {
        this.mMobileNum = mobile;
    }

    public String getGendle() {
        return mGendle;
    }

    public void setGendle(String mGendle) {
        this.mGendle = mGendle;
    }

    public String getSignature() {
        return mSignature;
    }

    public void setSignature(String mSignature) {
        this.mSignature = mSignature;
    }

    public String getUsrName() {
        return mUsrName;
    }

    public void setUsrName(String mUsrName) {
        this.mUsrName = mUsrName;
    }


    public String getBirthday() {
        return mBirthday;
    }

    public void setBirthday(String mBirthday) {
        this.mBirthday = mBirthday;
    }

    public String getPortraitPath() {
        return mPortraitPath;
    }

    public void setPortraitPath(String mPortraitPath) {
        this.mPortraitPath = mPortraitPath;
    }

    public GeoData getLocation() {
        return mLocation;
    }

    public void setLocation(GeoData mLocation) {
        this.mLocation = mLocation;
    }

    public String getSpeedDateId() {
        return mSpeedDateId;
    }

    public void setSpeedDateId(String speedDateId) {
        this.mSpeedDateId = speedDateId;
    }

    public String getDynamicDataId() {
        return mDynamicDataId;
    }

    public void setDynamicDataId(String dynamicDataId) {
        this.mDynamicDataId = dynamicDataId;
    }


    public int getUserDateState() {
        return mUserDateState;
    }

    public void setUserDateState(int newState) {
        this.mUserDateState = newState;
    }
}
