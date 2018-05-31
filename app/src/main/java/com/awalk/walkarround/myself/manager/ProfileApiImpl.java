/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.awalk.walkarround.myself.manager;

import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.awalk.walkarround.Location.manager.LocationManager;
import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.base.task.TaskUtil;
import com.awalk.walkarround.main.parser.WalkArroundJsonResultParser;
import com.awalk.walkarround.myself.model.MyDynamicInfo;
import com.awalk.walkarround.myself.task.CreateDynDataTask;
import com.awalk.walkarround.myself.task.QueryDynDataTask;
import com.awalk.walkarround.myself.task.UpdateDynDataTask;
import com.awalk.walkarround.myself.util.ProfileOnResultListener;
import com.awalk.walkarround.myself.util.ProfileUtil;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.http.HttpTaskBase;
import com.awalk.walkarround.util.http.HttpUtil;
import com.awalk.walkarround.util.http.ThreadPoolManager;

/**
 *
 */
public class ProfileApiImpl extends ProfileApiAbstract {

    private Logger mLogger = Logger.getLogger(ProfileApiImpl.class.getSimpleName());

    private HttpTaskBase.onResultListener mCreateDynDataTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {
        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_CREATE_DYNC_DATA)) {
                updateCurUserDynData(null);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {
        }
    };

    private HttpTaskBase.onResultListener mUpdateDynDataTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {
        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_UPDATE_DYNC_DATA)) {

            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {
        }
    };

    private HttpTaskBase.onResultListener mQueryDynDataTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {
        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            mLogger.w("mQueryDynDataTaskListener : " + (String) object);
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_QUERY_DYNC_DATA)) {
                String datingStatus = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_DATING_STATUS);
                if (TextUtils.isEmpty(datingStatus)) {
                    mLogger.d("datingStatus != null");
                    createCurUserDynData(null);
                } else {
                    mLogger.d("datingStatus == null");
                    updateCurUserDynData(null);
                }
            } else {
                mLogger.e("!!! queryCurUserDynData return fail result.");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {
        }
    };


    @Override
    public void updateGendle(String value) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        user.setFetchWhenSave(true);
        user.put(ProfileUtil.REG_KEY_GENDER, value);
        user.saveInBackground();
    }

    @Override
    public void updateSignature(String newSignature, final AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();
        user.setFetchWhenSave(true);
        user.put(ProfileUtil.REG_KEY_SIGNATURE, newSignature);
        user.saveInBackground(new SaveCallback() {

            @Override
            public void done(AVException e) {
                if (e == null) {
                    mLogger.d("Update user signature successful.");
                    listener.onSuccess(null);
                } else {
                    mLogger.d("Update user signature failed.");
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
    public void updateUsername(final String username, final AsyncTaskListener listener) throws Exception {
        AVUser user = AVUser.getCurrentUser();

        user.put(ProfileUtil.REG_KEY_USER_NAME, username);
        user.setFetchWhenSave(true);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //user.setUsername(username);
                    mLogger.d("Update user signature successful.");
                    listener.onSuccess(null);
                } else {
                    int code = e.getCode();
                    if (code == 0) {
                        mLogger.d("Update user signature successful.");
                        listener.onSuccess(null);
                    } else {
                        mLogger.d("Update user signature failed.");
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

    /**
     * Step 1: query if current user has dynamic data at first;
     * Step 2: If step_1 query data, user queried item. Otherwise, create a new dynamic data;
     * Step 3: Save data in background;
     * Step 4: Query current user data again and return dynamic data's object ID;
     *
     */
    @Override
    public void updateDynamicData(MyDynamicInfo dynamicInfo, AsyncTaskListener listener) throws Exception {
        if (dynamicInfo == null) {
            mLogger.w("updateDynamicData infor is NULL.");
            if (listener != null) {
                listener.onFailed(new AVException(-1, "updateDynamicData dynamicInfo is NULL."));
            }
            return;
        }

        mLogger.w("updateDynamicData start.");

        //geoData != null mean location mananger get MAP information
        GeoData geoData = LocationManager.getInstance(WalkArroundApp.getInstance()).getCurrentLoc();
        if (geoData != null) {
            if (ProfileManager.getInstance().getMyProfile() == null) {
                if (listener != null) {
                    listener.onFailed(new AVException(-1, "ProfileManager.getInstance().getMyProfile() is NULL."));
                }
                return;
            }

            GeoData curGeo = ProfileManager.getInstance().getMyProfile().getLocation();
            //curGeo != null means we already set location information to profile manager.
            if (curGeo == null) {
                queryCurUserDynData(listener);
            } else {
                updateCurUserDynData(listener);
            }
        } else {
            mLogger.w("updateDynamicData fail: there is no MAP information from MAP SDK.");
            if (listener != null) {
                listener.onFailed(new AVException(-1, "updateDynamicData here is no MAP information from MAP SDK."));
            }
        }
    }

    @Override
    public void queryCurUserDynData(AsyncTaskListener listener) {
        mLogger.d("queryCurUserDynData");

        ThreadPoolManager.getPoolManager().addAsyncTask(new QueryDynDataTask(WalkArroundApp.getInstance(),
                new ProfileOnResultListener(mQueryDynDataTaskListener, listener),
                HttpUtil.HTTP_FUNC_QUERY_DYNC_DATA,
                HttpUtil.HTTP_TASK_QUERY_DYNC_DATA,
                QueryDynDataTask.getParams(ProfileManager.getInstance().getCurUsrObjId()),
                TaskUtil.getTaskHeader()));
    }

    private void createCurUserDynData(AsyncTaskListener listener) {
        mLogger.d("createCurUserDynData");

        ThreadPoolManager.getPoolManager().addAsyncTask(new CreateDynDataTask(WalkArroundApp.getInstance(),
                new ProfileOnResultListener(mCreateDynDataTaskListener, listener),
                HttpUtil.HTTP_FUNC_CREATE_DYNC_DATA,
                HttpUtil.HTTP_TASK_CREATE_DYNC_DATA,
                CreateDynDataTask.getParams(ProfileManager.getInstance().getCurUsrObjId()),
                TaskUtil.getTaskHeader()));
    }

    private void updateCurUserDynData(AsyncTaskListener listener) {
        GeoData curGeo = ProfileManager.getInstance().getMyProfile().getLocation();

        if (curGeo != null) {
            mLogger.d("updateCurUserDynData");
            ThreadPoolManager.getPoolManager().addAsyncTask(new UpdateDynDataTask(WalkArroundApp.getInstance(),
                    new ProfileOnResultListener(mUpdateDynDataTaskListener, listener),
                    HttpUtil.HTTP_FUNC_UPDATE_DYNC_DATA,
                    HttpUtil.HTTP_TASK_UPDATE_DYNC_DATA,
                    UpdateDynDataTask.getParams(ProfileManager.getInstance().getCurUsrObjId(), curGeo.getLatitude(), curGeo.getLongitude()),
                    TaskUtil.getTaskHeader()));
        }
    }

    @Override
    public void updatePortrait(String path, final AsyncTaskListener listener) throws Exception {
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
    private void refreshLocationData(final GeoData input,final AsyncTaskListener listener) {
        if (input == null) {
            return;
        }

        final AVUser user = AVUser.getCurrentUser();
        AVObject origLocation = (AVObject) user.get(ProfileUtil.REG_KEY_LOCATION_EX);

        if (origLocation != null) {

            GetCallback callback = new GetCallback<AVObject>() {

                @Override
                public void done(AVObject avObject, AVException e) {
                    final AVObject post = avObject;
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
                    } else {
                    listener.onFailed(e);
                }
            }
        } ;

        AVQuery<AVObject> query = new AVQuery<AVObject>(AppConstant.TABLE_LOCATION_INFOR);
        query.getInBackground(origLocation.getObjectId(), callback);
    }

}

    /*
     * Upload location data
     */

    private void newLocationData(GeoData input, final AsyncTaskListener listener) {
        if (input == null) {
            return;
        }

        final AVUser user = AVUser.getCurrentUser();

        //Create a new location data if there is no original data
        final AVObject objLocation = new AVObject(AppConstant.TABLE_LOCATION_INFOR);
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
