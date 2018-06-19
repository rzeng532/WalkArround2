package com.awalk.walkarround.message.iview;

import com.awalk.walkarround.base.BaseView;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.ResponseInfo;

/**
 * ConversationView
 * Date: 2018-06-10
 *
 * @author mass
 */
public interface ConversationView extends BaseView {

    /**
     * 查询匹配中走伴信息
     *
     * @param isSuccess
     * @param speedDateInfo
     */
    void querySpeedDateIdResult(boolean isSuccess, DynamicRecord speedDateInfo);

    /**
     * 取消匹配中走伴信息
     *
     * @param isSuccess
     * @param result
     */
    void cancelSpeedDateResult(boolean isSuccess, MessageSessionBaseModel listDO, ResponseInfo result);

}
