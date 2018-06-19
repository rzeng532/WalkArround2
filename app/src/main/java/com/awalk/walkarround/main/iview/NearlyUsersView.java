package com.awalk.walkarround.main.iview;

import java.util.List;

import com.awalk.walkarround.base.BaseView;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.retrofit.model.ContactsList;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.FriendsList;
import com.awalk.walkarround.util.http.HttpTaskBase.TaskResult;

/**
 * AppMainView
 * Date: 2018-06-10
 *
 * @author mass
 */
public interface NearlyUsersView extends BaseView {

    /**
     * 设置为喜欢结果
     *
     * @param isSuccess
     * @param result
     */
    void likeSomeoneResult(boolean isSuccess, DynamicRecord result);

}
