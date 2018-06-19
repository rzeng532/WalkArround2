package com.awalk.walkarround.message.presenter;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.message.iview.BuildMessageView;
import com.awalk.walkarround.retrofit.ApiListener;
import com.awalk.walkarround.retrofit.ApiManager;
import com.awalk.walkarround.retrofit.model.ResponseInfo;
import com.awalk.walkarround.util.http.HttpUtil;

/**
 * 构建消息
 * Date: 2018-06-10
 *
 * @author mass
 */
public class BuildMessagePresenter extends BasePresenter<BuildMessageView> {

    /**
     * @param speedDateId
     * @param color
     */
    public void updateSpeedDateColor(String speedDateId, String color) {
        ApiManager.setColor(speedDateId, color, new ApiListener<ResponseInfo>() {
            @Override
            public void onSuccess(String code, ResponseInfo data) {
                if (mView != null) {
                    mView.updateSpeedDateColorResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.updateSpeedDateColorResult(false, null);
                }
            }
        });
    }
}
