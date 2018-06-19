package com.awalk.walkarround.myself.iview;

import com.awalk.walkarround.base.BaseView;

/**
 * EditStrProfileInfoView
 * Date: 2018-06-18
 *
 * @author mass
 */
public interface EditStrProfileInfoView extends BaseView {
    /**
     * 更新名字
     *
     * @param isSuccess
     */
    void updateUsernameResult(boolean isSuccess);

    /**
     * 更新签名
     *
     * @param isSuccess
     */
    void updateSignatureResult(boolean isSuccess);
}
