/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.manager;

import android.os.Environment;

import com.avos.avoscloud.*;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AsyncTaskListener;

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
    public void updateSignature(String newSignature, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_SIGNATURE, newSignature);
        user.saveInBackground(new SaveCallback() {

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
    public void updateBirthday(String birth) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_BIRTH_DAY, birth);
        user.saveInBackground();
    }

    @Override
    public void updateUsername(final String username, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_USER_NAME, username);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    user.setUsername(username);
                    listener.onSuccess();
                } else {
                    int code = e.getCode();
                    if (code == 0) {
                        listener.onSuccess();
                    } else {
                        listener.onFailed(e);
                    }
                }
            }
        });
    }

    @Override
    public void updatePortrait(String path, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        if (null == user) {
            return;
        }

        final AVFile orignalFile = user.getAVFile(ProfileUtil.REG_KEY_PORTRAIT);

        AVFile file = AVFile.withAbsoluteLocalPath(user.getMobilePhoneNumber(), path);
        user.put(REG_KEY_PORTRAIT, file);
        //TODO: maybe we need a callback here.
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //If new portrait file update success, we will delete old one.
                    if (orignalFile != null) {
                        Runnable deleteRunnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    orignalFile.delete();
                                } catch (AVException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        };
                        Thread deleteTask = new Thread(deleteRunnable);
                        deleteTask.start();
                    }
                    listener.onSuccess();
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }
}
