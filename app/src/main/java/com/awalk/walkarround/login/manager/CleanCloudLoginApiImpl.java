package com.awalk.walkarround.login.manager;

import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.base.task.TaskUtil;
import com.awalk.walkarround.login.task.CheckIfVerifiedTask;
import com.awalk.walkarround.login.util.LoginConstant;
import com.awalk.walkarround.main.parser.WalkArroundJsonResultParser;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.myself.util.ProfileUtil;
import com.awalk.walkarround.util.AppSharedPreference;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.http.HttpTaskBase;
import com.awalk.walkarround.util.http.HttpUtil;
import com.awalk.walkarround.util.http.ThreadPoolManager;

/**
 * Created by Richard on 2015/12/6.
 */
public class CleanCloudLoginApiImpl extends LoginApiAbstract {

    private AsyncTaskListener mInvokerListener = null;
    private AVException mException;
    private static Logger logger = Logger.getLogger(CleanCloudLoginApiImpl.class.getSimpleName());

    private HttpTaskBase.onResultListener mRegisteTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_REGISTE)) {
                String usrPhone = WalkArroundJsonResultParser.parseRequireCode((String)object, HttpUtil.HTTP_PARAM_PHONE);
                if(!TextUtils.isEmpty(usrPhone)
                        && LoginManager.getInstance().getPhoneNum().equalsIgnoreCase(usrPhone)) {
                    //If server reponse user details(like user name, it means server created a new record.)
                    mInvokerListener.onSuccess(null);
                } else {
                    AVUser user = new AVUser();
                    user.setUsername(LoginManager.getInstance().getUserName());
                    user.setPassword(LoginManager.getInstance().getPassword());
                    user.setMobilePhoneNumber(LoginManager.getInstance().getPhoneNum());
                    user.put(ProfileUtil.REG_KEY_GENDER, LoginManager.getInstance().getGender());

                    user.signUpInBackground(new SignUpCallback() {
                        public void done(AVException e) {
                            if (e == null) {
                                mInvokerListener.onSuccess(null);
                                //setCurrentAccount(userName, phoneNum,  password);
                            } else {
                                mInvokerListener.onFailed(e);
                            }
                        }
                    });
                }
            } else {
                logger.e("Code : " + resultCode + ", String: " + threadId);
                mInvokerListener.onFailed(mException);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    public void doRegister(final AsyncTaskListener listener) {
        //Get user information from manager. Email is empty now.
        doRegister(LoginManager.getInstance().getPhoneNum(),
                LoginManager.getInstance().getPassword(),
                LoginManager.getInstance().getUserName(),
                LoginManager.getInstance().getGender(),
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
                    if(listener != null) {
                        listener.onSuccess(null);
                    }
                    //TODO: we should get current user here.
                    //setCurrentAccount(null, phone, password);
                } else {
                    if(listener != null) {
                        listener.onFailed(e);
                    }
                }
            }
        });
    }

    @Override
    public void doLogout() throws Exception {
        AVUser avUser = AVUser.getCurrentUser();
        if(avUser != null) {
            avUser.logOut();
            ProfileManager.getInstance().setCurAccountLoginState(false);
        }
    }

    @Override
    public void doRegister(final String phoneNum, final String password, final String userName, final String gender, AsyncTaskListener listener) {

        mInvokerListener = listener;
        mException = null;

        final AVUser user = new AVUser();
        user.setUsername(LoginManager.getInstance().getUserName());
        user.setPassword(LoginManager.getInstance().getPassword());
        user.setMobilePhoneNumber(LoginManager.getInstance().getPhoneNum());
        user.put(ProfileUtil.REG_KEY_GENDER, LoginManager.getInstance().getGender());

        user.signUpInBackground(new SignUpCallback() {
            public void done(AVException e) {
                if (e == null) {
                    mInvokerListener.onSuccess(null);
                    //setCurrentAccount(userName, phoneNum,  password);
                } else {
                    if ((e.getCode() == AVException.ACCOUNT_ALREADY_LINKED
                            || e.getCode() == AVException.USER_MOBILE_PHONENUMBER_TAKEN)
                            && user.isMobilePhoneVerified() == false) {
                        //Check if there is a unverified user with the same phone number on server.
                        mException = e;
                        ThreadPoolManager.getPoolManager().addAsyncTask(
                                new CheckIfVerifiedTask(WalkArroundApp.getInstance(),
                                        mRegisteTaskListener,
                                        HttpUtil.HTTP_FUNC_REGISTE,
                                        HttpUtil.HTTP_TASK_REGISTE,
                                        CheckIfVerifiedTask.getParams(userName, password, gender, phoneNum),
                                        TaskUtil.getTaskHeader()));
                    } else {
                        mInvokerListener.onFailed(e);
                    }
                }
            }
        });
    }

    @Override
    public int getLoginState() {
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null && currentUser.isMobilePhoneVerified()) {
            logger.d("## loginByMobilePhoneNumberInBackground");
            //Login again via current account
            currentUser.loginByMobilePhoneNumberInBackground(AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, ""),
                    AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PASSWORD, ""), new LogInCallback() {

                        @Override
                        public void done(AVUser avUser, AVException e) {

                            logger.e("loginByMobilePhoneNumberInBackground Done: exception e " + ((e == null) ? "== null" : "!= null"));

                            if(e == null) {
                                ProfileManager.getInstance().setCurAccountLoginState(true);
                            }
                        }
                    });

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
                    listener.onSuccess(null);
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
