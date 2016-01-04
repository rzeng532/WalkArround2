package com.example.walkarround.myself.manager;

import android.text.TextUtils;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.myself.model.MyDynamicInfo;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AsyncTaskListener;

/**
 * Created by Richard on 2015/12/7.
 */
public class ProfileManager {
    private static ProfileManager mProfileManager;
    private static ProfileApiAbstract mProfileApi;

    public static ProfileManager getInstance() {
        if (mProfileManager == null) {
            synchronized (LoginManager.class) {
                if (mProfileManager == null) {
                    mProfileManager = new ProfileManager();
                    mProfileApi = new ProfileApiImpl();
                }
            }
        }

        return mProfileManager;
    }

    /*
     * Get current user profile information
     */
    public MyProfileInfo getMyProfile() {
        MyProfileInfo myProfileInfo = new MyProfileInfo();
        AVUser avUser = AVUser.getCurrentUser();

        //Set the mobile number as user name while user name is empty
        if(TextUtils.isEmpty(avUser.getUsername())) {
            myProfileInfo.setUsrName(avUser.getMobilePhoneNumber());
        } else {
            myProfileInfo.setUsrName(avUser.getUsername());
        }

        //Set mobile number
        myProfileInfo.setMobileNum(avUser.getMobilePhoneNumber());

        //Set portrait URL.
        AVFile portraitURL = avUser.getAVFile(ProfileUtil.REG_KEY_PORTRAIT);
        if(portraitURL != null && !TextUtils.isEmpty(portraitURL.getUrl())) {
            myProfileInfo.setPortraitPath(portraitURL.getUrl());
        } else {
            myProfileInfo.setPortraitPath(""); //Set portrait path as empty.
        }

        //Set gendle
        myProfileInfo.setGendle(avUser.getString(ProfileUtil.REG_KEY_GENDER));

        //Set birthday
        myProfileInfo.setBirthday(avUser.getString(ProfileUtil.REG_KEY_BIRTH_DAY));

        //Set signature
        myProfileInfo.setSignature(avUser.getString(ProfileUtil.REG_KEY_SIGNATURE));

        myProfileInfo.setLocation(new GeoData((AVObject) avUser.get(ProfileUtil.REG_KEY_LOCATION_EX)));

        return myProfileInfo;
    }

    /*
     * Update portrait
     */
    public void updatePortrait(String path, AsyncTaskListener listener) {
        try {
            mProfileApi.updatePortrait(path, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Update birthday
     */
    public void updateBirthday(String birth) {
        try {
            mProfileApi.updateBirthday(birth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Update gendle
     */
    public void updateGendle(String value) {
        try {
            mProfileApi.updateGendle(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Update signature
     */
    public void updateSignature(String signature, AsyncTaskListener listener) {
        try {
            mProfileApi.updateSignature(signature, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Update user name
     */
    public void updateUsername(String username, AsyncTaskListener listener) {
        try {
            mProfileApi.updateUsername(username, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Update user location information.
     */
    public void updateUserLocation(GeoData location, AsyncTaskListener listener) {
        try {
            mProfileApi.updateLocation(location, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDynamicData(MyDynamicInfo dynData, AsyncTaskListener listener) {
        try {
            mProfileApi.updateDynamicData(dynData, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
