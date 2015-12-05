package com.example.walkarround.login.manager;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.*;
import com.example.walkarround.R;
import com.example.walkarround.login.LoginConstant;
import com.example.walkarround.util.AppSharedPreference;

/**
 * Created by Richard on 2015/11/25.
 * <p/>
 * This class work for register and login on "login" package.
 */
public class LoginManager extends LoginManagerAbstract {

    /* Class values */

    private String mStrNickName = null;
    private String mStrPhoneNum = null;
    private String mStrPassword = null;

    //Get instance
    private static LoginManager mLoginInstance = null;

    public static LoginManager getInstance() {
        if (mLoginInstance == null) {
            synchronized (LoginManager.class) {
                if (mLoginInstance == null) {
                    mLoginInstance = new LoginManager();
                }
            }
        }

        return mLoginInstance;
    }

    /* Get & set method for local fields */
    public String getNickName() {
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

    public void setPassword(String mStrPassword) {
        this.mStrPassword = mStrPassword;
    }

    public void doRegister(final RegAndLoginListener listener) {
        //Get user information from manager. Email is empty now.
        doRegister(LoginManager.getInstance().getPhoneNum(),
                LoginManager.getInstance().getPassword(),
                LoginManager.getInstance().getNickName(),
                "",
                listener);
    }

    /* Implement abstract methods */
    @Override
    public void init() throws Exception {

    }

    @Override
    public void onDestroy() throws Exception {

    }

    @Override
    public void doLogin(final String phone, final String password, final RegAndLoginListener listener) throws Exception {

        AVUser.loginByMobilePhoneNumberInBackground(phone, password, new LogInCallback() {
            public void done(AVUser user, AVException e) {
                if (user != null) {
                    listener.onSuccess();

                    //TODO: we should get current user here.
                    setCurrentAccount(null, phone, password);
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    @Override
    public void doLogout() throws Exception {

    }

    @Override
    public void doRegister(final String phoneNum, final String  password, final String userName, String email, final RegAndLoginListener listener) {

        AVUser user = new AVUser();
        user.setUsername(userName);
        user.setPassword(password);
        //user.setEmail(email);
        user.setMobilePhoneNumber(phoneNum);
        //user.put(LoginConstant.REG_KEY_NICK_NAME, userName);

        user.signUpInBackground(new SignUpCallback() {
            public void done(AVException e) {
                if (e == null) {
                    listener.onSuccess();
                    setCurrentAccount(userName, phoneNum,  password);
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    @Override
    public int getLoginState() {
        return 0;
    }

    @Override
    protected void setLoginState(int newValue) throws Exception {

    }

    @Override
    public String getCurrentUserName() {
        String strUserName = null;
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            strUserName = currentUser.getMobilePhoneNumber();
        } else {
            strUserName = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
        }

        return strUserName;
    }

    @Override
    public void setCurrentUser() {

    }

    @Override
    public void createAccountWithCode(String code, final RegAndLoginListener listener) {
        AVUser.verifyMobilePhoneInBackground(code, new AVMobilePhoneVerifyCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    listener.onSuccess();
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    @Override
    public void findPasswordViaSMS(String strPhoneNum, RegAndLoginListener findPwdListener) throws Exception {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) throws Exception {

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

        return context.getString(strId);
    }

    /*
     * If the input value is null, we will skip to set this value.
     */
    private void setCurrentAccount(String username, String mobile, String password) {

        if(username != null) {
            AppSharedPreference.putString(AppSharedPreference.ACCOUNT_USERNAME, username);
        }
        if(mobile != null) {
            AppSharedPreference.putString(AppSharedPreference.ACCOUNT_PHONE, mobile);
        }
        if(password != null) {
            AppSharedPreference.putString(AppSharedPreference.ACCOUNT_PASSWORD, password);
        }
    }

    public void doLogin(RegAndLoginListener listener) {
        //TODO: We should encode user name and password !!
        String strName = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_USERNAME, "");
        String strPsd = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PASSWORD, "");

        if(!TextUtils.isEmpty(strName) && !TextUtils.isEmpty(strPsd)) {
            try {
                doLogin(strName, strPsd, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
