package com.example.walkarround.message.adapter;

import android.content.Context;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.MessageConstant.ConversationType;
import com.example.walkarround.message.util.MessageConstant.TopState;
import com.example.walkarround.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationListAdapter extends BaseConversationListAdapter {
    public static final int FIXED_ENTRANCES = 3;
    private static final Logger logger = Logger.getLogger(ConversationListAdapter.class.getSimpleName());

    public ConversationListAdapter(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * 是否全选中了
     *
     * @return
     */
    @Override
    public boolean isSelectAll() {
        return getChosenItemCount() == (getCount() - FIXED_ENTRANCES);
    }

    /**
     * 全选
     */
    @Override
    public void setSelectAll() {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (canSelectable(getItem(i))) {
                addToChosenPositionList(i);
            }
        }
    }

    /**
     * 是否可选中
     *
     * @return
     */
    @Override
    protected boolean canSelectable(MessageSessionBaseModel item) {
        return item.getItemType() == ConversationType.GENERAL || item.getTop() == TopState.TOP;
    }

    /**
     * 取消选中的置顶消息
     *
     * @return 选中的消息中的通知类消息
     */
    public List<MessageSessionBaseModel> cancelTopMsg(List<Long> deleteList) {
        HashMap<Long, Boolean> deleteIds = new HashMap<Long, Boolean>();
        if (deleteList != null) {
            for (long thread : deleteList) {
                deleteIds.put(thread, true);
            }
        }
        //返回取消置顶的通知消息列表
        List<Long> choseItems = getChosenItemsThreadId();
        List<MessageSessionBaseModel> notifyMsgList = new ArrayList<MessageSessionBaseModel>();
        for (Long item : choseItems) {
            MessageSessionBaseModel msgITem = getChosenItems(item);
            if (deleteIds.containsKey(msgITem.getThreadId())) {
                continue;
            }
            msgITem.setTop(TopState.NOT_TOP);
            if (msgITem.getItemType() == ConversationType.NOTICES_MSG) {
                notifyMsgList.add(msgITem);
            } else {
                removeFromChosenPositionList(item);
            }
        }
        deleteSelectedItem();
        return notifyMsgList;
    }

}
