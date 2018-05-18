/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.setting.manager;

import com.awalk.walkarround.login.manager.LoginManager;
import com.awalk.walkarround.main.activity.NearlyUsersFragment;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.myself.manager.ProfileManager;

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
        WalkArroundMsgManager.onDestroy();
        ProfileManager.onDestroy();
        NearlyUsersFragment.getInstance().clearNearlyUserList();
        LoginManager.getInstance().doLogout();
    }
}
