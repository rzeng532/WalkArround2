package com.awalk.walkarround.message.presenter;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.message.iview.ConversationView;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.retrofit.ApiListener;
import com.awalk.walkarround.retrofit.ApiManager;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.ResponseInfo;
import com.awalk.walkarround.util.http.HttpUtil;

/**
 * ConversationPresenter
 * Date: 2018-06-10
 *
 * @author mass
 */
public class ConversationPresenter extends BasePresenter<ConversationView> {

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
     * 取消走走
     *
     * @param speedDateId
     */
    public void cancelSpeedDate(final MessageSessionBaseModel listDO, String speedDateId) {
        ApiManager.cancelSpeedDate(speedDateId, new ApiListener<ResponseInfo>() {
            @Override
            public void onSuccess(String code, ResponseInfo data) {
                if (HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code)) {
                    ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_INIT);
                    ProfileManager.getInstance().setSpeedDateId(null);
                }
                if (mView != null) {
                    mView.cancelSpeedDateResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code),
                            listDO, data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.cancelSpeedDateResult(false, listDO, null);
                }
            }
        });
    }
}
