package com.awalk.walkarround.message.iview;

import com.awalk.walkarround.base.BaseView;
import com.awalk.walkarround.retrofit.model.ResponseInfo;

/**
 * BuildMessageView
 * Date: 2018-06-10
 *
 * @author mass
 */
public interface BuildMessageView extends BaseView {

    /**
     * 更新走伴颜色
     *
     * @param isSuccess
     * @param result
     */
    void updateSpeedDateColorResult(boolean isSuccess, ResponseInfo result);

}
