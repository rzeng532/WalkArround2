package com.awalk.walkarround.message.listener;

import android.view.View;
import com.awalk.walkarround.message.model.ChatMsgBaseInfo;

public interface MessageItemListener {

    /**
     * 点击事件
     * 
     * @param clickedItemView
     * @param clickedMessage
     */
    public void messageItemOnClick(View clickedItemView, ChatMsgBaseInfo clickedMessage);

    /**
     * 重发消息
     * 
     */
    public void messageResend(ChatMsgBaseInfo clickedMessage);

    /**
     * 选择模式变化了
     * 
     * @param isSelectMode
     */
    public void onSelectModeChange(boolean isSelectMode);

    /**
     * 选择的个数
     * 
     * @param selectCount
     */
    public void onSelectCountChange(int selectCount, int oldCount);

    /**
     * 收藏消息
     *
     * @param clickedMessage
     */
    public void onMsgCollect(ChatMsgBaseInfo clickedMessage);

    /**
     * 删除消息
     *
     * @param clickedMessage
     */
    public void onMsgDelete(ChatMsgBaseInfo clickedMessage);

    /**
     * 转发消息
     *
     * @param clickedMessage
     */
    public void onMsgForward(ChatMsgBaseInfo clickedMessage);

}
