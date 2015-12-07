package com.example.walkarround.setting.manager;

import com.example.walkarround.login.manager.LoginManager;

/**
 * Created by Richard on 2015/12/7.
 */
public class ProfileManager {
    private static ProfileManager mProfileManager = new ProfileManager();

    public static ProfileManager getInstance() {
        if (mProfileManager == null) {
            synchronized (LoginManager.class) {
                if (mProfileManager == null) {
                    mProfileManager = new ProfileManager();
                }
            }
        }

        return mProfileManager;
    }

    private void updateMyProfile() {

    }

    private void updateProfileIcon() {

    }
}
