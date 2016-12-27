package com.example.walkarround.login.manager;

import android.content.Context;
import android.text.TextUtils;

import android.util.Log;
import com.avos.avoscloud.*;
import com.example.walkarround.R;
import com.example.walkarround.login.util.LoginConstant;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.*;

/**
 * Created by Richard on 2015/11/25.
 * <p>
 * This class work for register and login on "login" package.
 */
public class LoginManager {

    /* Class values */

    private String mStrNickName = null;
    private String mIntGender = null;
    private String mStrPhoneNum = null;
    private String mStrPassword = null;
    private static LoginApiAbstract mLoginApi = null;
    private Logger logger = null;
    //Get instance
    private static LoginManager mLoginManager = null;

    public static LoginManager getInstance() {
        if (mLoginManager == null) {
            synchronized (LoginManager.class) {
                if (mLoginManager == null) {
                    mLoginManager = new LoginManager();
                    mLoginApi = new CleanCloudLoginApiImpl();
                }
            }
        }

        return mLoginManager;
    }

    private LoginManager() {
        logger = Logger.getLogger(LoginManager.class.getSimpleName());
    }

    /* Get & set method for local fields */
    public String getUserName() {
        return mStrNickName;
    }

    public void setNickName(String mStrNickName) {
        this.mStrNickName = mStrNickName;
    }

    public String getPhoneNum() {
        return mStrPhoneNum;
    }

    public void setPhoneNum(String mStrPhoneNum) {
        this.mStrPhoneNum = mStrPhoneNum;
    }

    public String getPassword() {
        return mStrPassword;
    }

    public void setGender(String gender) { this.mIntGender = gender;}

    public String getGender() { return mIntGender; }

    public void setPassword(String mStrPassword) {
        this.mStrPassword = mStrPassword;
    }

    public void doRegister(final AsyncTaskListener listener) {
        //Get user information from manager. Email is empty now.
        if (mLoginApi != null) {
            mLoginApi.doRegister(LoginManager.getInstance().getPhoneNum(),
                    LoginManager.getInstance().getPassword(),
                    LoginManager.getInstance().getUserName(),
                    LoginManager.getInstance().getGender(),
                    listener);
            setCurrentAccount(LoginManager.getInstance().getUserName(),
                    LoginManager.getInstance().getPhoneNum(),
                    LoginManager.getInstance().getPassword());
        }
    }

    public void clearAllData() {
        mStrNickName = null;
        mIntGender = null;
        mStrPhoneNum = null;
        mStrPassword = null;
    }

    /*
     * String <--> error code
     */
    public String getErrStringViaErrorCode(Context context, int errCode) {
        int strId = R.string.err_unknow;
        switch (errCode) {
            case AVException.ACCOUNT_ALREADY_LINKED:
            case AVException.USER_MOBILE_PHONENUMBER_TAKEN:
                strId = R.string.err_account_already_exist;
                break;
            case AVException.USER_ID_MISMATCH:
            case AVException.USERNAME_PASSWORD_MISMATCH:
                strId = R.string.err_username_password_mismatch;
                break;
            case AVException.USER_DOESNOT_EXIST:
                strId = R.string.err_username_donot_exist;
                break;
            default:
                strId = R.string.err_unknow;
        }

        //Test
        return context.getString(strId) + ", Error code: " + errCode;
    }

    /*
     * If the input value is null, we will skip to set this value.
     */
    private void setCurrentAccount(String username, String mobile, String password) {

        if (username != null) {
            AppSharedPreference.putString(AppSharedPreference.ACCOUNT_USERNAME, username);
        }
        if (mobile != null) {
            AppSharedPreference.putString(AppSharedPreference.ACCOUNT_PHONE, mobile);
        }
        if (password != null) {
            AppSharedPreference.putString(AppSharedPreference.ACCOUNT_PASSWORD, password);
        }
    }

    /*
     * We will user phone to do login step. And it will do in background.
     * This API designed for login via SP data. Like auto login.
     */
    public void doLogin(AsyncTaskListener listener) {
        //TODO: We should encode user name and password !!
        String strPhone = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
        String strPsd = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PASSWORD, "");

        if (!TextUtils.isEmpty(strPhone) && !TextUtils.isEmpty(strPsd) && CommonUtils.validatePhoneNum(strPhone)) {
            doLogin(strPhone, strPsd, listener);
        }
    }

    /*
    * We will user phone to do login step. And it will do in background.
    * This API is designed for manual login.
    */
    public void doLogin(String phone, String password, AsyncTaskListener listener) {

        //TODO: We should check password valid or NOT.
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password) && CommonUtils.validatePhoneNum(phone)) {
            try {
                if (mLoginApi != null) {
                    mLoginApi.doLogin(phone, password, listener);
                    setCurrentAccount(null, phone, password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getCurrentUserName() {
        if (mLoginApi != null) {
            return mLoginApi.getCurrentUserName();
        }

        return null;
    }

    public void createAccountWithCode(String code, final AsyncTaskListener listener) {
        if (mLoginApi != null) {
            mLoginApi.createAccountWithCode(code, listener);
        }
    }

    public void setCurrentUser() {
        AVUser currentUser = AVUser.getCurrentUser();

        if (currentUser != null) {
            String username = currentUser.getUsername();
            if (!TextUtils.isEmpty(username)) {
                AppSharedPreference.putString(AppSharedPreference.ACCOUNT_USERNAME, username);

                logger.d("current user name: " + username);
                logger.d("current user mobile: " + currentUser.getMobilePhoneNumber());
            }
        }
    }

    public boolean isLogined() {
        return (mLoginApi.getLoginState() == LoginConstant.LOGIN_STATE ? true : false);
    }

    public void doLogout() {
        try {
            mLoginApi.doLogout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLocationData() {
        AVUser user = AVUser.getCurrentUser();
        if(user == null) {
            return;
        }
        AVObject origObj = user.getAVObject(ProfileUtil.REG_KEY_LOCATION_EX);
        if(origObj == null) {
            return;
        }

        AVQuery query = new AVQuery(AppConstant.TABLE_LOCATION_INFOR);
        query.getInBackground(origObj.getObjectId(), new GetCallback() {
            @Override
            public void done(AVObject avObject, AVException e) {
                Log.v("GeoData", "done ");
            }

            @Override
            protected void internalDone0(Object o, AVException e) {
                if(e != null) {
                    //Return if resule is not success.
                    return;
                }

                if (o != null) {
                    AVUser user = AVUser.getCurrentUser();
                    user.put(ProfileUtil.REG_KEY_LOCATION_EX, (AVObject)o);
                }
            }
        });
    }
}
