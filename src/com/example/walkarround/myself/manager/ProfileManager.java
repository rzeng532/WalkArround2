package com.example.walkarround.myself.manager;

import com.example.walkarround.login.manager.LoginManager;

/**
 * Created by Richard on 2015/12/7.
 */
public class ProfileManager {
    private static ProfileManager mProfileManager;
    private static ProfileApiAbstract mProfileApi;

    public static ProfileManager getInstance() {
        if (mProfileManager == null) {
            synchronized (LoginManager.class) {
                if (mProfileManager == null) {
                    mProfileManager = new ProfileManager();
                    mProfileApi = new ProfileApiImpl();
                }
            }
        }

        return mProfileManager;
    }

    private void updateMyProfile() {

    }

    private void updatePortrait(String path) {
        try {
            mProfileApi.updatePortrait(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
