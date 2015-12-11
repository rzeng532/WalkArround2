/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.manager;

import com.avos.avoscloud.AVUser;
import com.example.walkarround.myself.util.ProfileUtil;

import static com.example.walkarround.myself.util.ProfileUtil.*;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public class ProfileApiImpl extends ProfileApiAbstract {
    @Override
    public void updateGendle(int value) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_GENDER, value);
        user.saveInBackground();
    }

    @Override
    public void updateSignature(String newSignature) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_SIGNATURE, newSignature);
        user.saveInBackground();
    }

    @Override
    public void updateBirthday(String birth) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_BIRTH_DAY, birth);
        user.saveInBackground();
    }

    @Override
    public void updateUsername(String birth) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_USER_NAME, birth);
        user.saveInBackground();
    }

    @Override
    public void updatePortrait(String path) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_PORTRAIT, path);
        user.saveInBackground();
    }
}
