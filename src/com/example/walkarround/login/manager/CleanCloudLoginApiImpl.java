package com.example.walkarround.login.manager;

import android.widget.EditText;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;
import com.example.walkarround.R;
import com.example.walkarround.login.util.LoginConstant;
import com.example.walkarround.util.AppSharedPreference;
import com.example.walkarround.util.AsyncTaskListener;

/**
 * Created by Richard on 2015/12/6.
 */
public class CleanCloudLoginApiImpl extends LoginApiAbstract {

    public void doRegister(final AsyncTaskListener listener) {
        //Get user information from manager. Email is empty now.
        doRegister(LoginManager.getInstance().getPhoneNum(),
                LoginManager.getInstance().getPassword(),
                LoginManager.getInstance().getUserName(),
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
    public void doLogin(final String phone, final String password, final AsyncTaskListener listener) throws Exception {

        AVUser.loginByMobilePhoneNumberInBackground(phone, password, new LogInCallback() {
            public void done(AVUser user, AVException e) {
                if (user != null) {
                    listener.onSuccess();

                    //TODO: we should get current user here.
                    //setCurrentAccount(null, phone, password);
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    @Override
    public void doLogout() throws Exception {
        AVUser avUser = AVUser.getCurrentUser();
        avUser.logOut();
    }

    @Override
    public void doRegister(final String phoneNum, final String  password, final String userName, String email, final AsyncTaskListener listener) {

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
                    //setCurrentAccount(userName, phoneNum,  password);
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    @Override
    public int getLoginState() {
        AVUser currentUser = AVUser.getCurrentUser();
        if(currentUser != null) {
            //Login again via current account
            try {
                currentUser.logIn(AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, ""),
                        AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PASSWORD, ""));
            } catch (AVException e) {
                e.printStackTrace();
            }
            return LoginConstant.LOGIN_STATE;
        } else {
            return LoginConstant.LOGOUT_STATE;
        }
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
    public void createAccountWithCode(String code, final AsyncTaskListener listener) {
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
    public void findPasswordViaSMS(String strPhoneNum, AsyncTaskListener findPwdListener) throws Exception {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) throws Exception {

    }
}
