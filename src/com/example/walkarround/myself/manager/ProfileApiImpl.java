/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.manager;

import com.avos.avoscloud.*;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppConstant;
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
        user.setFetchWhenSave(true);
        user.put(REG_KEY_GENDER, value);
        user.saveInBackground();
    }

    @Override
    public void updateSignature(String newSignature, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        user.setFetchWhenSave(true);
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
        user.setFetchWhenSave(true);
        user.put(REG_KEY_BIRTH_DAY, birth);
        user.saveInBackground();
    }

    @Override
    public void updateUsername(final String username, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(REG_KEY_USER_NAME, username);
        user.setFetchWhenSave(true);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //user.setUsername(username);
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
    public void updateLocation(GeoData input, AsyncTaskListener listener) throws Exception {
        //Check data
        if (input == null) {
            listener.onFailed(null);
        }

        //Get current user and old location data.
        AVUser user = AVUser.getCurrentUser();
        AVObject origLocation = (AVObject) user.get(REG_KEY_LOCATION_EX);

        if (origLocation == null) {
            newLocationData(input, listener);
        } else {
            refreshLocationData(input, listener);
        }
    }

    @Override
    public void updatePortrait(String path, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        if (null == user) {
            return;
        }

        final AVFile orignalFile = user.getAVFile(ProfileUtil.REG_KEY_PORTRAIT);

        AVFile file = AVFile.withAbsoluteLocalPath(user.getMobilePhoneNumber(), path);
        //user.setFetchWhenSave(true);
        user.put(REG_KEY_PORTRAIT, file);
        //TODO: maybe we need a callback here.
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //If new portrait file update success, we will delete old one.
                    if (orignalFile != null) {
                        orignalFile.deleteInBackground();
                    }

                    listener.onSuccess();
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    /*
     * Refresh current user data.
     */
    private void refreshLocationData(GeoData input, AsyncTaskListener listener) {
        if (input == null) {
            return;
        }

        AVUser user = AVUser.getCurrentUser();
        AVObject origLocation = (AVObject) user.get(REG_KEY_LOCATION_EX);

        if (origLocation != null) {

            AVQuery<AVObject> query = new AVQuery<AVObject>(AppConstant.TABLE_LOCATION_INFOR);
            query.getInBackground(origLocation.getObjectId(), new GetCallback() {

                @Override
                protected void internalDone0(Object o, AVException e) {
                    if(e == null) {
                        AVObject post = (AVObject) o;
                        if (post != null) {
                            post.put(REG_KEY_LOCATION, new AVGeoPoint(input.getLatitude(), input.getLongitude()));
                            post.put(REG_KEY_LOCATION_ADDR, input.getAddrInfor());
                            post.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        user.setFetchWhenSave(true);
                                        user.put(REG_KEY_LOCATION_EX, post);
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
                                    } else {
                                        listener.onFailed(e);
                                    }
                                }
                            });
                        }
                    } else {
                        listener.onFailed(e);
                    }
                }

                @Override
                public void done(AVObject avObject, AVException e) {

                }
            });
        }
    }

    /*
     * Upload location data
     */

    private void newLocationData(GeoData input, AsyncTaskListener listener) {
        if (input == null) {
            return;
        }

        AVUser user = AVUser.getCurrentUser();

        //Create a new location data if there is no original data
        AVObject objLocation = new AVObject(AppConstant.TABLE_LOCATION_INFOR);
        AVGeoPoint geoInfor = new AVGeoPoint(input.getLatitude(), input.getLongitude());
        objLocation.setFetchWhenSave(true);
        objLocation.put(REG_KEY_LOCATION, geoInfor);
        objLocation.put(REG_KEY_LOCATION_ADDR, input.getAddrInfor());
        objLocation.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //Update user location information.
                    user.setFetchWhenSave(true);
                    user.put(REG_KEY_LOCATION_EX, objLocation);
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
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

}
