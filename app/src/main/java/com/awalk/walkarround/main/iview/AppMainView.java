package com.awalk.walkarround.main.iview;

import com.awalk.walkarround.base.BaseView;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.FriendsList;
import com.awalk.walkarround.util.http.HttpTaskBase.TaskResult;

import java.util.List;

/**
 * AppMainView
 * Date: 2018-06-10
 *
 * @author mass
 */
public interface AppMainView extends BaseView {

    /**
     * 获取本地缓存Session数据
     *
     * @param resultCode
     * @param requestCode
     * @param resultList
     */
    void loadSessionResult(TaskResult resultCode, String requestCode, List<MessageSessionBaseModel> resultList);

    /**
     * 定位成功
     */
    void onLocation(boolean isSuccess, Exception exception);

    /**
     * 更新动态信息结果
     *
     * @param isSuccess 成功／失败
     * @param record
     */
    void updateDynamicDataResult(boolean isSuccess, DynamicRecord record);


    /**
     * 获取走伴列表结果
     *
     * @param isSuccess   成功／失败
     * @param friendsList
     */
    void getFriendListResult(boolean isSuccess, FriendsList friendsList);

    /**
     * 查询匹配中结果
     *
     * @param isSuccess     成功／失败
     * @param speedDateInfo
     */
    void querySpeedDateIdResult(boolean isSuccess, DynamicRecord speedDateInfo);

    /**
     * 查询附近的人结果
     *
     * @param isSuccess      成功／失败
     * @param nearlyUserList 结果
     */
    void queryNearlyUsersResult(boolean isSuccess, List<ContactInfo> nearlyUserList);

}
