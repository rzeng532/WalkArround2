package com.example.walkarround.message.listener;


import com.example.walkarround.message.model.MessageSessionBaseModel;

public interface ConversationItemListener {
    public void conversationItemOnClick(int position, MessageSessionBaseModel listDO);
    public void onSelectModeChanged(boolean isInSelectMode);
    public void onDeleteConversationItem(MessageSessionBaseModel listDO);
}
