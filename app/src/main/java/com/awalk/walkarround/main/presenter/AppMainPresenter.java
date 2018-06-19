package com.awalk.walkarround.main.presenter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.Location.manager.LocationManager;
import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.main.iview.AppMainView;
import com.awalk.walkarround.main.parser.WalkArroundJsonResultParser;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.task.AsyncTaskLoadSession;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.retrofit.ApiListener;
import com.awalk.walkarround.retrofit.ApiManager;
import com.awalk.walkarround.retrofit.model.ContactsList;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.FriendsList;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.http.HttpTaskBase;
import com.awalk.walkarround.util.http.HttpUtil;
import com.awalk.walkarround.util.http.ThreadPoolManager;

/**
 * AppMainPresenter
 * Date: 2018-06-10
 *
 * @author mass
 */
public class AppMainPresenter extends BasePresenter<AppMainView> {

    /**
     * 获取本地缓冲会话Session
     *
     * @param context
     */
    public void loadSession(Context context) {
        ThreadPoolManager.getPoolManager().addAsyncTask(
                new AsyncTaskLoadSession(context, 0, Integer.MAX_VALUE,
                        new HttpTaskBase.onResultListener() {
                            @Override
                            public void onPreTask(String requestCode) {

                            }

                            @Override
                            public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
                                if (mView != null) {
                                    mView.loadSessionResult(resultCode, requestCode, (List<MessageSessionBaseModel>) object);
                                }
                            }

                            @Override
                            public void onProgress(int progress, String requestCode) {

                            }
                        }));

    }

    /**
     * 当前位置
     *
     * @param context
     */
    public void locateCurPosition(Context context) {
        LocationManager.getInstance(context).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN,
                new AsyncTaskListener() {
                    @Override
                    public void onSuccess(Object data) {
                        if (mView != null) {
                            mView.onLocation(true, null);
                        }
                    }

                    @Override
                    public void onFailed(AVException e) {
                        if (mView != null) {
                            mView.onLocation(false, e);
                        }
                    }
                });
    }

    /**
     * 根据当前平台数据返回结果更新位置状态信息
     */
    public void updateDynamicData() {
        GeoData geoData = LocationManager.getInstance(WalkArroundApp.getInstance()).getCurrentLoc();
        if (geoData != null) {
            if (ProfileManager.getInstance().getMyProfile() == null) {
                if (mView != null) {
                    mView.updateDynamicDataResult(false, null);
                }
                return;
            }

            GeoData curGeo = ProfileManager.getInstance().getMyProfile().getLocation();
            //curGeo != null means we already set location information to profile manager.
            if (curGeo == null) {
                // 查询平台是否有记录
                queryCurUserDynData(ProfileManager.getInstance().getCurUsrObjId());
            } else {
                // 更新平台数据
                updateDynamicData(ProfileManager.getInstance().getCurUsrObjId(), curGeo);
            }
        } else {
            if (mView != null) {
                mView.updateDynamicDataResult(false, null);
            }
        }

    }

    /**
     * 更新平台位置状态信息
     *
     * @param userObjId
     * @param curGeo
     */
    public void updateDynamicData(String userObjId, GeoData curGeo) {
        ApiManager.updateDynamicData(userObjId, curGeo.getLatitude(), curGeo.getLongitude(),
                new ApiListener<DynamicRecord>() {
                    @Override
                    public void onSuccess(String code, DynamicRecord data) {
                        if (mView != null) {
                            mView.updateDynamicDataResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (mView != null) {
                            mView.updateDynamicDataResult(false, null);
                        }
                    }
                });

    }

    /**
     * 查询用户位置信息
     */
    private void queryCurUserDynData(String userObjId) {
        ApiManager.queryUserDynamicData(userObjId, new ApiListener<DynamicRecord>() {
            @Override
            public void onSuccess(String code, DynamicRecord data) {
                if (mView != null) {
                    String datingStatus = data.getResult().getDatingStatus();
                    if (TextUtils.isEmpty(datingStatus)) {
                        createCurUserDynData(ProfileManager.getInstance().getCurUsrObjId());
                    } else {
                        GeoData curGeo = ProfileManager.getInstance().getMyProfile().getLocation();
                        updateDynamicData(ProfileManager.getInstance().getCurUsrObjId(), curGeo);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.updateDynamicDataResult(false, null);
                }
            }
        });

    }

    // 创建用户动态数据
    private void createCurUserDynData(String userObjId) {
        ApiManager.createUserDynamicData(userObjId, null);
    }

    /**
     * 朋友列表
     *
     * @param userObjId
     * @param count
     */
    public void getFriendList(String userObjId, int count) {
        ApiManager.getFriendList(userObjId, count, new ApiListener<FriendsList>() {
            @Override
            public void onSuccess(String code, FriendsList data) {
                if (mView != null) {
                    mView.getFriendListResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.getFriendListResult(false, null);
                }
            }
        });
    }

    /**
     * 查询当前用户走走记录
     *
     * @param userObjId
     */
    public void querySpeedDate(String userObjId) {
        ApiManager.querySpeedDate(userObjId, new ApiListener<DynamicRecord>() {
            @Override
            public void onSuccess(String code, DynamicRecord data) {
                if (mView != null) {
                    mView.querySpeedDateIdResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.querySpeedDateIdResult(false, null);
                }
            }
        });
    }

    /**
     * 查询附近好友列表
     *
     * @param dynamicDataId
     */
    public void queryNearlyUsers(String dynamicDataId) {
        ApiManager.queryNearlyUsers(dynamicDataId, new ApiListener<ContactsList>() {
            @Override
            public void onSuccess(String code, ContactsList data) {
                if (mView != null) {
                    mView.queryNearlyUsersResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.queryNearlyUsersResult(false, null);
                }
            }
        });
    }
}
