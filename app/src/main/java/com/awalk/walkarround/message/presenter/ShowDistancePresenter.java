package com.awalk.walkarround.message.presenter;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.Location.manager.LocationManager;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.message.iview.ShowDistanceView;
import com.awalk.walkarround.retrofit.ApiListener;
import com.awalk.walkarround.retrofit.ApiManager;
import com.awalk.walkarround.retrofit.model.UserCoordinate;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.http.HttpUtil;

/**
 * ShowDistancePresenter
 * Date: 2018-06-10
 *
 * @author mass
 */
public class ShowDistancePresenter extends BasePresenter<ShowDistanceView> {

    /**
     * 查询好友位置信息
     *
     * @param usrId
     */
    public void queryUsrCoordinate(String usrId) {
        ApiManager.getUserCoordinate(usrId, new ApiListener<UserCoordinate>() {
            @Override
            public void onSuccess(String code, UserCoordinate data) {
                if (mView != null) {
                    mView.queryUsrCoordinateResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.queryUsrCoordinateResult(false, null);
                }
            }
        });

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
                            mView.onLocation(true);
                        }
                    }

                    @Override
                    public void onFailed(AVException e) {
                        if (mView != null) {
                            mView.onLocation(false);
                        }
                    }
                });
    }

}
