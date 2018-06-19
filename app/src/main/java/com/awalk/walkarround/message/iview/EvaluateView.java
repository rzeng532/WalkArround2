package com.awalk.walkarround.message.iview;

import com.awalk.walkarround.base.BaseView;
import com.awalk.walkarround.retrofit.model.ResponseInfo;

/**
 * EvaluateView
 * Date: 2018-06-10
 *
 * @author mass
 */
public interface EvaluateView extends BaseView {

    /**
     * 评价结果
     *
     * @param isSuccess
     * @param result
     */
    void evaluateResult(boolean isSuccess, ResponseInfo result);


    /**
     *  添加朋友
     *
     * @param isSuccess
     * @param result
     */
    void addFriendResult(boolean isSuccess, ResponseInfo result);

}
