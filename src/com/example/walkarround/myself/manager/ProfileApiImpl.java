/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.manager;

import com.avos.avoscloud.*;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.myself.model.MyDynamicInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;

import java.util.List;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public class ProfileApiImpl extends ProfileApiAbstract {
    @Override
    public void updateGendle(String value) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        user.setFetchWhenSave(true);
        user.put(ProfileUtil.REG_KEY_GENDER, value);
        user.saveInBackground();
    }

    @Override
    public void updateSignature(String newSignature, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        user.setFetchWhenSave(true);
        user.put(ProfileUtil.REG_KEY_SIGNATURE, newSignature);
        user.saveInBackground(new SaveCallback() {

            @Override
            public void done(AVException e) {
                if (e == null) {
                    listener.onSuccess(null);
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
        user.put(ProfileUtil.REG_KEY_BIRTH_DAY, birth);
        user.saveInBackground();
    }

    @Override
    public void updateUsername(final String username, AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(ProfileUtil.REG_KEY_USER_NAME, username);
        user.setFetchWhenSave(true);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //user.setUsername(username);
                    listener.onSuccess(null);
                } else {
                    int code = e.getCode();
                    if (code == 0) {
                        listener.onSuccess(null);
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
        AVObject origLocation = (AVObject) user.get(ProfileUtil.REG_KEY_LOCATION_EX);

        if (origLocation == null) {
            newLocationData(input, listener);
        } else {
            refreshLocationData(input, listener);
        }
    }

    /*
     * Step 1: query if current user has dynamic data at first;
     * Step 2: If step_1 query data, user queried item. Otherwise, create a new dynamic data;
     * Step 3: Save data in background;
     * Step 4: Query current user data again and return dynamic data's object ID;
     *
     */
    @Override
    public void updateDynamicData(MyDynamicInfo dynamicInfo, AsyncTaskListener listener) throws Exception {
        if (dynamicInfo == null) {
            return;
        }

        //Query dynamic data at first
        AVQuery<AVObject> query = new AVQuery<AVObject>(AppConstant.TABLE_DYNAMIC_USER_DATA);
        query.whereEqualTo(ProfileUtil.DYN_DATA_USER_ID, AVUser.getCurrentUser());
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (e == null) {
                    //Query success
                    AVObject objLocation = null;
                    if (list != null && list.size() > 0) {
                        //There is record, update original one.
                        objLocation = (AVObject) list.get(0); //There is only one record
                    } else {
                        //There is no record, create a new one.
                        objLocation = new AVObject(AppConstant.TABLE_DYNAMIC_USER_DATA);
                        objLocation.put(ProfileUtil.DYN_DATA_USER_ID, AVUser.getCurrentUser());
                    }

                    //Fill data
                    objLocation.put(ProfileUtil.DYN_DATA_ONLINE_STATE, dynamicInfo.getOnlineState());
                    objLocation.put(ProfileUtil.DYN_DATA_GEO, ProfileUtil.geodataConvert2AVObj(dynamicInfo.getCurGeo()));
                    objLocation.put(ProfileUtil.DYN_DATA_DATING_STATE, dynamicInfo.getDatingState());

                    //Update dynamic data to server.
                    objLocation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (listener == null) {
                                return;
                            }

                            if (e == null) {
                                //Update object successful, query current user's data again.
                                queryCurUserDynData(new AsyncTaskListener() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        if(data != null) {
                                            AVObject dynData = (AVObject) data;
                                            listener.onSuccess(dynData.getObjectId());
                                        } else {
                                            listener.onFailed(null);
                                        }
                                    }

                                    @Override
                                    public void onFailed(AVException e) {
                                        listener.onFailed(e);
                                    }
                                });
                            } else {
                                listener.onFailed(e);
                            }
                        }
                    });
                } else {
                    if (listener != null) {
                        listener.onFailed(e);
                    }
                }
            }
        });
    }

    @Override
    public void queryCurUserDynData(AsyncTaskListener listener) {

        if(listener == null) {
            return ;
        }

        //Query current user's dynamic data
        AVQuery<AVObject> query = new AVQuery<AVObject>(AppConstant.TABLE_DYNAMIC_USER_DATA);
        query.whereEqualTo(ProfileUtil.DYN_DATA_USER_ID, AVUser.getCurrentUser());
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (e == null) {
                    //Query success
                    AVObject objLocation = null;
                    if (list != null && list.size() > 0) {
                        //There is record, update original one.
                        objLocation = (AVObject) list.get(0); //There is only one record
                        listener.onSuccess(objLocation);
                    } else {
                        listener.onFailed(null);
                    }
                } else {
                    listener.onFailed(e);
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
        //user.setFetchWhenSave(true);
        user.put(ProfileUtil.REG_KEY_PORTRAIT, file);
        //TODO: maybe we need a callback here.
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //If new portrait file update success, we will delete old one.
                    if (orignalFile != null) {
                        orignalFile.deleteInBackground();
                    }

                    listener.onSuccess(null);
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
        AVObject origLocation = (AVObject) user.get(ProfileUtil.REG_KEY_LOCATION_EX);

        if (origLocation != null) {

            AVQuery<AVObject> query = new AVQuery<AVObject>(AppConstant.TABLE_LOCATION_INFOR);
            query.getInBackground(origLocation.getObjectId(), new GetCallback() {

                @Override
                protected void internalDone0(Object o, AVException e) {
                    if (e == null) {
                        AVObject post = (AVObject) o;
                        if (post != null) {
                            post.put(ProfileUtil.REG_KEY_LOCATION, new AVGeoPoint(input.getLatitude(), input.getLongitude()));
                            post.put(ProfileUtil.REG_KEY_LOCATION_ADDR, input.getAddrInfor());
                            post.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        user.setFetchWhenSave(true);
                                        user.put(ProfileUtil.REG_KEY_LOCATION_EX, post);
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                if (e == null) {
                                                    listener.onSuccess(null);
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
        objLocation.put(ProfileUtil.REG_KEY_LOCATION, geoInfor);
        objLocation.put(ProfileUtil.REG_KEY_LOCATION_ADDR, input.getAddrInfor());
        objLocation.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //Update user location information.
                    user.setFetchWhenSave(true);
                    user.put(ProfileUtil.REG_KEY_LOCATION_EX, objLocation);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                listener.onSuccess(null);
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
