/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.manager;

import android.os.Environment;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;
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
                int code = e.getCode();
                if (code == 0) {
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
    public void updateUsername(String birth, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_USER_NAME, birth);
        user.saveInBackground(new SaveCallback() {

            @Override
            public void done(AVException e) {
                if (e == null) {
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
    public void updatePortrait(String path) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        AVFile file = AVFile.withAbsoluteLocalPath(user.getMobilePhoneNumber() + ".png", path);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e != null) {
                    //TODO: update portrait file
                    //user.put(REG_KEY_PORTRAIT, path);
                    //user.saveInBackground();
                } else {
                    //上传成功
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                //打印进度
                System.out.println("uploading: " + percentDone);
            }
        });
    }
}
