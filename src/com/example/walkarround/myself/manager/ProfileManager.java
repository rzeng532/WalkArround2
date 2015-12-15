package com.example.walkarround.myself.manager;

import android.text.TextUtils;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.example.walkarround.login.manager.LoginManager;
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
     * 获取个人信息
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

        myProfileInfo.setMobileNum(avUser.getMobilePhoneNumber());

        AVFile portraitURL = avUser.getAVFile(ProfileUtil.REG_KEY_PORTRAIT);
        if(portraitURL != null && !TextUtils.isEmpty(portraitURL.getUrl())) {
            myProfileInfo.setPortraitPath(portraitURL.getUrl());
        } else {
            myProfileInfo.setPortraitPath(""); //Set portrait path as empty.
        }

        myProfileInfo.setGendle(avUser.getInt(ProfileUtil.REG_KEY_GENDER));
        myProfileInfo.setBirthday(avUser.getString(ProfileUtil.REG_KEY_BIRTH_DAY));
        myProfileInfo.setSignature(avUser.getString(ProfileUtil.REG_KEY_SIGNATURE));

        return myProfileInfo;
    }

    /*
     * 更新头像
     */
    public void updatePortrait(String path, AsyncTaskListener listener) {
        try {
            mProfileApi.updatePortrait(path, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 更新性别
     */
    public void updateBirthday(String birth) {
        try {
            mProfileApi.updateBirthday(birth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 更新性别
     */
    public void updateGendle(int value) {
        try {
            mProfileApi.updateGendle(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 更新签名
     */
    public void updateSignature(String signature, AsyncTaskListener listener) {
        try {
            mProfileApi.updateSignature(signature, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 更改用户名
     */
    public void updateUsername(String username, AsyncTaskListener listener) {
        try {
            mProfileApi.updateUsername(username, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
