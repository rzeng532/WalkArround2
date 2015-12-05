/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.login.manager;

import java.util.HashMap;

/**
 * Date: 2015-11-26
 *
 * @author Richard
 *
 * Abstract class for LoginManager.
 */
public abstract class LoginManagerAbstract {

    public abstract void init()
            throws Exception;

    public abstract void onDestroy()
            throws Exception;

    public abstract void doLogin(String userName, String password, final RegAndLoginListener listener)
            throws Exception;

    public abstract void doLogout()
            throws Exception;

    public abstract void doRegister(String phoneNum, String password, String userName,
                                    String email, RegAndLoginListener listener);

    public abstract int getLoginState();

    protected abstract void setLoginState(int newValue) throws Exception;

    public abstract String getCurrentUserName() ;

    public abstract void setCurrentUser();

    public abstract void createAccountWithCode(String code, final RegAndLoginListener listener);

    public abstract void findPasswordViaSMS(String strPhoneNum, final RegAndLoginListener findPwdListener) throws Exception;

    public abstract void changePassword(final String oldPassword, final String newPassword) throws Exception;
}
