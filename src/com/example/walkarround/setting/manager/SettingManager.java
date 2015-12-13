/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.setting.manager;

import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.myself.manager.ProfileApiAbstract;
import com.example.walkarround.myself.manager.ProfileApiImpl;
import com.example.walkarround.myself.manager.ProfileManager;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Richard
 */
public class SettingManager {

    private static SettingManager mSettingManager;

    public static SettingManager getInstance() {
        if (mSettingManager == null) {
            synchronized (LoginManager.class) {
                if (mSettingManager == null) {
                    mSettingManager = new SettingManager();
                }
            }
        }

        return mSettingManager;
    }

    public void doLogout() {
        LoginManager.getInstance().doLogout();
    }
}
