/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.login.manager;

import com.awalk.walkarround.util.AsyncTaskListener;

/**
 * Date: 2015-11-26
 *
 * @author Richard
 *
 * Abstract class for LoginManager.
 */
public abstract class LoginApiAbstract {

    public abstract void init()
            throws Exception;

    public abstract void onDestroy()
            throws Exception;

    public abstract void doLogin(String userName, String password, final AsyncTaskListener listener)
            throws Exception;

    public abstract void doLogout()
            throws Exception;

    public abstract void doRegister(String phoneNum, String password, String userName,
                                    String email, AsyncTaskListener listener);

    public abstract int getLoginState();

    protected abstract void setLoginState(int newValue) throws Exception;

    public abstract String getCurrentUserName() ;

    public abstract void createAccountWithCode(String code, final AsyncTaskListener listener);

    public abstract void findPasswordViaSMS(String strPhoneNum, final AsyncTaskListener findPwdListener) throws Exception;

    public abstract void changePassword(final String oldPassword, final String newPassword) throws Exception;
}
