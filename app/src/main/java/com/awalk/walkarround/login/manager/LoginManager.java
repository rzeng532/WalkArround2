package com.awalk.walkarround.login.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.avos.avoscloud.*;
import com.awalk.walkarround.R;
import com.awalk.walkarround.login.util.LoginConstant;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.myself.model.MyProfileInfo;
import com.awalk.walkarround.myself.util.ProfileUtil;
import com.awalk.walkarround.util.*;

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
        int strId = R.string.err_register_unknow;
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
                strId = R.string.err_register_unknow;
                break;
        }

        return context.getString(strId);
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
        MyProfileInfo profile = ProfileManager.getInstance().getMyProfile();

        if (profile != null) {
            String username = profile.getUsrName();
            if (!TextUtils.isEmpty(username)) {
                AppSharedPreference.putString(AppSharedPreference.ACCOUNT_USERNAME, username);

                logger.d("current user name: " + username);
                logger.d("current user mobile: " + profile.getMobileNum());
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
        query.getInBackground(origObj.getObjectId(), new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if(e != null) {
                    //Return if resule is not success.
                    return;
                }

                if (avObject != null) {
                    AVUser user = AVUser.getCurrentUser();
                    user.put(ProfileUtil.REG_KEY_LOCATION_EX, avObject);
                }
                Log.v("GeoData", "done ");
            }
        });
    }

    public void updatePassword(String oldPsw, String newPsw, AsyncTaskListener listener) {
        AVUser user = AVUser.getCurrentUser();

        if(user != null) {
            user.updatePasswordInBackground(oldPsw, newPsw, new UpdatePasswordCallback() {
                @Override
                public void done(AVException e) {
                    if(e == null) {
                        if(listener != null) {
                            listener.onSuccess(null);
                        }
                    } else {
                        if(listener != null) {
                            listener.onFailed(null);
                        }
                    }
                }
            });
        }
    }
}
