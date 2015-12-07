package com.example.walkarround.setting.model;

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
    int mGendle = 0; //0: men, 1: female

    public int getGendle() {
        return mGendle;
    }

    public void setGendle(int mGendle) {
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
}
