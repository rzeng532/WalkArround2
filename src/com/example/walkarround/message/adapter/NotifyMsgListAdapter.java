package com.example.walkarround.message.adapter;

import android.content.Context;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.util.Logger;

import java.util.List;

/**
 * 通知消息会话展示
 */
public class NotifyMsgListAdapter extends BaseConversationListAdapter {

    private static final Logger logger = Logger.getLogger(NotifyMsgListAdapter.class.getSimpleName());

    private boolean hasInit = false;

    public NotifyMsgListAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void setListData(List<MessageSessionBaseModel> list) {
        super.setListData(list);
        hasInit = true;
    }

    /**
     * 是否已经初始化数据
     *
     * @return
     */
    public boolean hasInitData() {
        return hasInit;
    }

}
