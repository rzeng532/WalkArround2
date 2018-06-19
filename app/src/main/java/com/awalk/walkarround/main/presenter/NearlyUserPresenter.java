package com.awalk.walkarround.main.presenter;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.main.iview.AppMainView;
import com.awalk.walkarround.main.iview.NearlyUsersView;
import com.awalk.walkarround.retrofit.ApiListener;
import com.awalk.walkarround.retrofit.ApiManager;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.util.http.HttpUtil;

/**
 * NearlyUserPresenter
 * Date: 2018-06-14
 *
 * @author mass
 */
public class NearlyUserPresenter extends BasePresenter<NearlyUsersView> {

    public void liseSomeone(String fromUserId, String toUserId) {
        ApiManager.likeSomeone(fromUserId, toUserId, new ApiListener<DynamicRecord>() {
            @Override
            public void onSuccess(String code , DynamicRecord data) {
                if (mView != null) {
                    mView.likeSomeoneResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.likeSomeoneResult(false, null);
                }
            }
        });
    }
}
