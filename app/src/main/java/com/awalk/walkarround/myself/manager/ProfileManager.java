package com.awalk.walkarround.myself.manager;

import android.text.TextUtils;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.login.manager.LoginManager;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.myself.model.MyDynamicInfo;
import com.awalk.walkarround.myself.model.MyProfileInfo;
import com.awalk.walkarround.myself.util.ProfileUtil;
import com.awalk.walkarround.retrofit.trace.HttpTrace;
import com.awalk.walkarround.util.AppSharedPreference;
import com.awalk.walkarround.util.AsyncTaskListener;

/**
 * Created by Richard on 2015/12/7.
 */
public class ProfileManager {
    private static ProfileManager mProfileManager;
    private static ProfileApiAbstract mProfileApi;
    private static MyProfileInfo myProfileInfo;

    private boolean mIsLogined = false;

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

    private void initMyProfile() {

        AVUser avUser = AVUser.getCurrentUser();
        if(avUser == null) {
            return;
        }

        myProfileInfo = new MyProfileInfo();

        //Set the mobile number as user name while user name is empty
        if(TextUtils.isEmpty(avUser.getUsername())) {
            myProfileInfo.setUsrName(avUser.getMobilePhoneNumber());
        } else {
            myProfileInfo.setUsrName(avUser.getUsername());
        }

        String storeAccount = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
        if (!storeAccount.equals(avUser.getMobilePhoneNumber())) {
            HttpTrace.handleHttpTraceInfor("login exception", "account_error",
                    "storeAccount:" + storeAccount+", AVUser.getMobilePhoneNumber:" + avUser.getMobilePhoneNumber());
        }
        //Set mobile number
        myProfileInfo.setMobileNum(avUser.getMobilePhoneNumber());

        //Set portrait URL.
        try{
            AVFile portraitURL = avUser.getAVFile(ProfileUtil.REG_KEY_PORTRAIT);
            if(portraitURL != null && !TextUtils.isEmpty(portraitURL.getUrl())) {
                myProfileInfo.setPortraitPath(portraitURL.getUrl());
            } else {
                myProfileInfo.setPortraitPath(""); //Set portrait path as empty.
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        //Set gendle
        myProfileInfo.setGendle(avUser.getString(ProfileUtil.REG_KEY_GENDER));

        //Set birthday
        myProfileInfo.setBirthday(avUser.getString(ProfileUtil.REG_KEY_BIRTH_DAY));

        //Set signature
        myProfileInfo.setSignature(avUser.getString(ProfileUtil.REG_KEY_SIGNATURE));

        //Don't init location information while init personal information.
        // Setter just be invoked by location listener on app main activity.
        //myProfileInfo.setLocation(new GeoData((AVObject) avUser.get(ProfileUtil.REG_KEY_LOCATION_EX)));
    }

    /*
     * Get current user profile information
     */
    public MyProfileInfo getMyProfile() {

        if (myProfileInfo == null) {
            synchronized (LoginManager.class) {
                if (myProfileInfo == null) {
                    initMyProfile();
                }
            }
        }

        return myProfileInfo;
    }

    public ContactInfo getMyContactInfo() {
        MyProfileInfo myProfileInfo = getMyProfile();

        ContactInfo infor = new ContactInfo();
        infor.setUsername(myProfileInfo.getUsrName());
        infor.setBirthday(myProfileInfo.getBirthday());
        infor.setGender(myProfileInfo.getGendle());
        infor.setMobilePhoneNumber(myProfileInfo.getMobileNum());
        infor.setSignature(myProfileInfo.getSignature());

        ContactInfo.PortraitEntity entry = infor.getPortrait();
        entry.setUrl(myProfileInfo.getPortraitPath());
        infor.setPortrait(entry);

        return infor;
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

    public String getCurUsrObjId() {
        AVUser avUser = AVUser.getCurrentUser();
        if(avUser != null) {
            return avUser.getObjectId();
        } else {
            return null;
        }
    }

    public int getCurUsrDateState() {
        if(myProfileInfo == null) {
            return -1;
        }
        return myProfileInfo.getUserDateState();
    }

    public void setCurUsrDateState(int state) {
        if(myProfileInfo == null) {
            return ;
        }
        myProfileInfo.setUserDateState(state);
    }

    public String getSpeedDateId() {
        if(myProfileInfo == null) {
            return null;
        }
        return myProfileInfo.getSpeedDateId();
    }

    public void setSpeedDateId(String speedDateId) {
        if(myProfileInfo == null) {
            return;
        }
        myProfileInfo.setSpeedDateId(speedDateId);
    }

    public static void onDestroy() {
        mProfileManager = null;
        myProfileInfo = null;
        mProfileApi = null;
    }

    public boolean getCurAccountLoginState() {
        return mProfileManager.mIsLogined;
    }

    public void setCurAccountLoginState(boolean newState) {
        mProfileManager.mIsLogined = newState;
    }
}
