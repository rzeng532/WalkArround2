package com.example.walkarround.myself.manager;

import com.avos.avoscloud.AVUser;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.myself.util.ProfileUtil;

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

        myProfileInfo.setUsrName(avUser.getUsername());
        myProfileInfo.setPortraitPath(avUser.getString(ProfileUtil.REG_KEY_PORTRAIT));
        myProfileInfo.setGendle(avUser.getInt(ProfileUtil.REG_KEY_GENDER));
        myProfileInfo.setBirthday(avUser.getString(ProfileUtil.REG_KEY_BIRTH_DAY));
        myProfileInfo.setSignature(avUser.getString(ProfileUtil.REG_KEY_SIGNATURE));

        return myProfileInfo;
    }

    /*
     * 更新头像
     */
    public void updatePortrait(String path) {
        try {
            mProfileApi.updatePortrait(path);
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
    public void updateSignature(String signature) {
        try {
            mProfileApi.updateSignature(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 更改用户名
     */
    public void updateUsername(String username) {
        try {
            mProfileApi.updateUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
