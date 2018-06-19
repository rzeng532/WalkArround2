package com.awalk.walkarround.message.iview;

import com.awalk.walkarround.base.BaseView;
import com.awalk.walkarround.retrofit.model.ResponseInfo;
import com.awalk.walkarround.retrofit.model.UserCoordinate;

/**
 * ShowDistanceView
 * Date: 2018-06-10
 *
 * @author mass
 */
public interface ShowDistanceView extends BaseView {
    /**
     * 查询结果
     * @param isSuccess
     * @param result
     */
    void queryUsrCoordinateResult(boolean isSuccess, UserCoordinate result);

    /**
     * 定位成功
     */
    void onLocation(boolean isSuccess);
}
