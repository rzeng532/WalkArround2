package com.example.walkarround.message.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.example.walkarround.message.activity.BuildMessageActivity;
import com.example.walkarround.message.listener.MessageItemListener;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.MessageConstant.MessageSendReceive;
import com.example.walkarround.message.util.MessageConstant.MessageState;
import com.example.walkarround.message.util.MessageConstant.MessageType;
import com.example.walkarround.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 消息内容展示 Date: 2015-03-16
 *
 * @author mss
 */
public class MessageDetailListAdapter extends BaseAdapter implements MessageDetailViewFactory.ItemListener {

    /* 消息类型种类数 */
    public final static int MESSAGE_TYPE_COUNT = 8 * 2 + 2;
    /* 纯文本消息 */
    public final static int MESSAGE_TYPE_PLAIN_TEXT_SEND = 0;
    public final static int MESSAGE_TYPE_PLAIN_TEXT_REC = 1;
    /* 图片消息 */
    public final static int MESSAGE_TYPE_PICTURE_SEND = 2;
    public final static int MESSAGE_TYPE_PICTURE_REC = 3;
    /* 音频消息 */
    public final static int MESSAGE_TYPE_AUDIO_SEND = 4;
    public final static int MESSAGE_TYPE_AUDIO_REC = 5;
    /* 视频消息 */
    public final static int MESSAGE_TYPE_VIDEO_SEND = 6;
    public final static int MESSAGE_TYPE_VIDEO_REC = 7;
    /* Vcard消息 */
    public final static int MESSAGE_TYPE_VCARD_SEND = 8;
    public final static int MESSAGE_TYPE_VCARD_REC = 9;
    /* 定位消息 */
    public final static int MESSAGE_TYPE_LOCATION_SEND = 10;
    public final static int MESSAGE_TYPE_LOCATION_REC = 11;
    /* 文件消息 */
    public final static int MESSAGE_TYPE_FILE_SEND = 12;
    public final static int MESSAGE_TYPE_FILE_REC = 13;
    /* git图片消息 */
    public final static int MESSAGE_TYPE_GIF_PICTURE_SEND = 14;
    public final static int MESSAGE_TYPE_GIF_PICTURE_REC = 15;

    public final static int MESSAGE_TYPE_SYSTEM = 16;
    public final static int MESSAGE_TYPE_MIX = 17;
    /* 消息List */
    private List<ChatMsgBaseInfo> mMessageInfoList = new ArrayList<ChatMsgBaseInfo>();

    /* 是否在选择模式 */
    private boolean isInSelectMode = false;
    /* 消息操作监听 */
    private MessageItemListener mItemListener;

    /* 选中的项目 */
    private HashMap<String, ChatMsgBaseInfo> mSelectedItemList = new HashMap<String, ChatMsgBaseInfo>();

    /*列表中顶部SMS消息ID,IM消息ID*/
    private long mLastChatId = 0;
    private long mLastSmsId = 0;
    /*列表中di部SMS消息ID,IM消息ID*/
    private long mBottomChatId = 0;
    private long mBottomSmsId = 0;
    /*创建消息 View*/
    private MessageDetailViewFactory mViewFactory;
    /*是否是群聊*/
    private boolean isGroupChat = false;

    private boolean isShowNickName = false;

    public MessageDetailListAdapter(Context context, MessageItemListener itemListener) {
        this.mItemListener = itemListener;
        mViewFactory = new MessageDetailViewFactory(context, this, false);
    }

    /**
     * 初始化消息列表
     *
     * @param messageInfoList
     */
    public void setMessageInfo(List<ChatMsgBaseInfo> messageInfoList) {
        mMessageInfoList.clear();
        mSelectedItemList.clear();
        if (isInSelectMode && mItemListener != null) {
            mItemListener.onSelectModeChange(false);
        }
        isInSelectMode = false;
        mLastChatId = 0;
        mLastSmsId = 0;
        mBottomChatId = 0;
        mBottomSmsId = 0;
        if (messageInfoList == null) {
            return;
        }
        mMessageInfoList.addAll(messageInfoList);
    }

    /**
     * 初始化消息列表
     *
     * @param messageInfoList
     */
    public void addUpMessageInfo(List<ChatMsgBaseInfo> messageInfoList) {
        if (messageInfoList == null) {
            return;
        }
        mMessageInfoList.addAll(0, messageInfoList);
    }

    /**
     * 初始化消息列表
     *
     * @param messageInfoList
     */
    public void addDownMessageInfo(List<ChatMsgBaseInfo> messageInfoList) {
        if (messageInfoList == null) {
            return;
        }
        mMessageInfoList.addAll(messageInfoList);
    }

    /**
     * 增加一条消息记录
     *
     * @param message
     */
    public void addMessage(ChatMsgBaseInfo message) {
        mMessageInfoList.add(message);
    }

    /**
     * 删除一条消息记录
     *
     * @param message
     */
    public void deleteMessage(ChatMsgBaseInfo message) {
        mMessageInfoList.remove(message);
    }

    /**
     * 删除一条消息记录
     *
     * @param messageId
     */
    public void deleteRcsMessage(long messageId) {
        int messageCount = mMessageInfoList.size();
        for (int i = messageCount - 1; i >= 0; i--) {
            ChatMsgBaseInfo message = mMessageInfoList.get(i);
            if (message.getMsgId() == messageId) {
                mMessageInfoList.remove(message);
                break;
            }
        }
    }

    /**
     * 删除一条定时消息记录
     *
     * @param messageId
     */
    public void deleteTimeMessageById(long messageId) {
        int messageCount = mMessageInfoList.size();
        for (int i = messageCount - 1; i >= 0; i--) {
            ChatMsgBaseInfo message = mMessageInfoList.get(i);
            if (message.getMsgId() == messageId && message.isTimeSendMsg()) {
                mMessageInfoList.remove(message);
                break;
            }
        }
    }

    /**
     * 删除选中的消息记录
     */
    public void deleteSelectedMessage() {
        for (ChatMsgBaseInfo value : mSelectedItemList.values()) {
            mMessageInfoList.remove(value);
        }
        mSelectedItemList.clear();
    }

    /**
     * 更新消息状态
     *
     * @param messageId
     * @param status
     */
    public void updateMessageStatus(long messageId, int status) {
        int messageCount = mMessageInfoList.size();
        if (messageCount < 1) {
            return;
        }
        for (int i = messageCount - 1; i >= 0; i--) {
            ChatMsgBaseInfo message = mMessageInfoList.get(i);
            if (message.getMsgId() == messageId && !message.isTimeSendMsg()) {
                message.setMsgState(status);
                if ((message.getSendReceive() == MessageSendReceive.MSG_SEND)
                        && (status == MessageState.MSG_STATE_SENT)
                        && message.isBurnAfterMsg()) {
                    // 阅后即焚消息
                    final ChatMsgBaseInfo chatMessage = message;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mMessageInfoList == null) {
                                return;
                            }
                            deleteMessage(chatMessage);
                            notifyDataSetChanged();
                        }
                    }, BuildMessageActivity.MSG_BURN_AFTER_READ_DURATION);
                }
                break;
            }
        }
    }

    /**
     * 根据id查找消息
     *
     * @param messageId
     */
    public ChatMsgBaseInfo getRcsMessageById(long messageId) {
        int messageCount = mMessageInfoList.size();
        if (messageCount < 1) {
            return null;
        }
        for (int i = messageCount - 1; i >= 0; i--) {
            ChatMsgBaseInfo message = mMessageInfoList.get(i);
            if (message.getMsgId() == messageId && !message.isTimeSendMsg()) {
                return message;
            }
        }
        return null;
    }

    /**
     * 设置是否选中模式
     *
     * @param isInSelectMode
     */
    public void setInSelectMode(boolean isInSelectMode) {
        this.isInSelectMode = isInSelectMode;
        if (!isInSelectMode) {
            setSelectForAll(false);
        }
        if (mItemListener != null) {
            mItemListener.onSelectModeChange(isInSelectMode);
        }
    }

    /**
     * 获取所有选中的消息
     *
     * @return
     */
    public List<ChatMsgBaseInfo> getSelectedMsgList() {
        ChatMsgBaseInfo[] list = new ChatMsgBaseInfo[mSelectedItemList.values().size()];
        mSelectedItemList.values().toArray(list);
        return Arrays.asList(list);
    }

    public void clearSelectedMsgList() {
        mSelectedItemList.clear();
    }

    /**
     * 设置顶部消息ID
     *
     * @param chatId
     * @param smsId
     */
    public void setLastMessagesId(long chatId, long smsId) {
        mLastChatId = chatId;
        mLastSmsId = smsId;
    }

    /**
     * 设置底部消息ID
     *
     * @param chatId
     * @param smsId
     */
    public void setBottomMessagesId(long chatId, long smsId) {
        mBottomChatId = chatId;
        mBottomSmsId = smsId;
    }

    /**
     * 获取顶部IM消息ID
     *
     * @return
     */
    public long getLastChatId() {
        return mLastChatId;
    }

    /**
     * 获取顶部SMS消息ID
     *
     * @return
     */
    public long getLastSmsId() {
        return mLastSmsId;
    }

    public long getBottomChatId() {
        return mBottomChatId;
    }

    /**
     * 获取顶部SMS消息ID
     *
     * @return
     */
    public long getBottomSmsId() {
        return mBottomSmsId;
    }

    public void dismissDialog() {
        mViewFactory.dismissDialog();
    }

    /*设置是否群聊*/
    public void setGroupChat(boolean isGroupChat) {
        this.isGroupChat = isGroupChat;
    }

    /**
     * 是否显示昵称
     *
     * @param isShowNickName
     */
    public void setShowNickName(boolean isShowNickName) {
        this.isShowNickName = isShowNickName;
    }

    public boolean isShowNickName() {
        return isShowNickName;
    }

    /**
     * 设置昵称集合
     *
     * @param nickNameMap
     */
    public void setNickNameMap(HashMap<String, String> nickNameMap) {
        mViewFactory.setNickNameMap(nickNameMap);
    }

    /**
     * 更新群昵称
     *
     * @param phoneNumber
     * @param nickName
     */
    public void updateNickNameMap(String phoneNumber, String nickName) {
        mViewFactory.updateNickNameMap(phoneNumber, nickName);
    }

    /**
     * 情况联系人显示名和头像信息，重新获取
     */
    public void clearDisplayName() {
        if (isGroupChat) {
            for (ChatMsgBaseInfo message : mMessageInfoList) {
                message.setDisplayName(null);
            }
            mViewFactory.clearCachName();
        }
    }

    @Override
    public int getCount() {
        return mMessageInfoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int messageType = MESSAGE_TYPE_PLAIN_TEXT_SEND;
        ChatMsgBaseInfo message = getItem(position);
        if (message.isBurnAfterMsg()) {
            messageType = MESSAGE_TYPE_PLAIN_TEXT_SEND;
            if (message.getSendReceive() == MessageSendReceive.MSG_RECEIVE) {
                messageType += 1;
            }
            return messageType;
        }
        switch (message.getMsgType()) {
            case MessageType.MSG_TYPE_TEXT:
                messageType = MESSAGE_TYPE_PLAIN_TEXT_SEND;
                break;
            case MessageType.MSG_TYPE_AUDIO:
                messageType = MESSAGE_TYPE_AUDIO_SEND;
                break;
            case MessageType.MSG_TYPE_VIDEO:
                messageType = MESSAGE_TYPE_VIDEO_SEND;
                break;
            case MessageType.MSG_TYPE_IMAGE:
                messageType = MESSAGE_TYPE_PICTURE_SEND;
                if (MessageUtil.isGifFile(message.getFilepath())) {
                    messageType = MESSAGE_TYPE_GIF_PICTURE_SEND;
                } else if (MessageUtil.isGifFile(message.getThumbUrlPath())) {
                    messageType = MESSAGE_TYPE_GIF_PICTURE_SEND;
                }
                break;
            case MessageType.MSG_TYPE_MAP:
                messageType = MESSAGE_TYPE_LOCATION_SEND;
                break;
            case MessageType.MSG_TYPE_NOTIFICATION:
                messageType = MESSAGE_TYPE_SYSTEM;
                break;
            default:
                break;
        }
        if (messageType != MESSAGE_TYPE_SYSTEM && messageType != MESSAGE_TYPE_MIX) {
            if (message.getSendReceive() == MessageSendReceive.MSG_RECEIVE) {
                messageType += 1;
            }
        }
        return messageType;
    }

    @Override
    public int getViewTypeCount() {
        return MESSAGE_TYPE_COUNT;
    }

    @Override
    public ChatMsgBaseInfo getItem(int position) {
        return mMessageInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void messageItemOnClick(View clickedItemView, ChatMsgBaseInfo clickedMessage) {
        if (isInSelectMode) {
            // 项目选择状态切换
            ChatMsgBaseInfo chatMessageInfo = clickedMessage;
            boolean isChecked = !chatMessageInfo.isChecked();
            chatMessageInfo.setChecked(isChecked);
            String key = Integer.toString(BuildMessageActivity.MSG_FROM_TYPE_RCS);
            key += "-" + chatMessageInfo.getMsgId();
            if (isChecked) {
                mSelectedItemList.put(key, chatMessageInfo);
            } else {
                mSelectedItemList.remove(key);
            }
            notifyDataSetChanged();
            if (mItemListener != null) {
                int selectedCount = mSelectedItemList.keySet().size();
                int oldCount = isChecked ? (selectedCount - 1) : (selectedCount + 1);
                mItemListener.onSelectCountChange(selectedCount, oldCount);
            }
        } else if (mItemListener != null) {
            mItemListener.messageItemOnClick(clickedItemView, clickedMessage);
        }
    }

    @Override
    public void messageResend(ChatMsgBaseInfo clickedMessage) {
        if (mItemListener != null) {
            mItemListener.messageResend(clickedMessage);
        }
    }

    @Override
    public boolean onItemLongClicked(View clickedItemView, ChatMsgBaseInfo clickedMessage) {
        // 选择模式切换
        if (isInSelectMode) {
            // 取消选择模式
            if (mItemListener != null) {
                mItemListener.onSelectModeChange(false);
            }
            isInSelectMode = false;
            setSelectForAll(false);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMsgCollect(View clickedView, ChatMsgBaseInfo clickedMessage) {
        if (mItemListener != null) {
            mItemListener.onMsgCollect(clickedMessage);
        }
    }

    @Override
    public void onMsgDelete(ChatMsgBaseInfo clickedMessage) {
        if (mItemListener != null) {
            mItemListener.onMsgDelete(clickedMessage);
        }
    }

    @Override
    public void onMsgMore(View clickedView, ChatMsgBaseInfo clickedMessage) {
        isInSelectMode = true;
        if (mItemListener != null) {
            mItemListener.onSelectModeChange(true);
        }
        messageItemOnClick(clickedView, clickedMessage);
    }

    @Override
    public void onMsgForward(View clickedView, ChatMsgBaseInfo clickedMessage) {
        if (mItemListener != null) {
            mItemListener.onMsgForward(clickedMessage);
        }
        String key = Integer.toString(BuildMessageActivity.MSG_FROM_TYPE_RCS);
        key += "-" + clickedMessage.getMsgId();
        mSelectedItemList.put(key, clickedMessage);
    }

    /**
     * 全选中/ 取消全部选中
     */
    public void setSelectForAll(boolean isChecked) {
        int oldSelectedCount = mSelectedItemList.keySet().size();
        if (isChecked) {
            for (ChatMsgBaseInfo messageInfo : mMessageInfoList) {
                messageInfo.setChecked(isChecked);
                String key = Integer.toString(BuildMessageActivity.MSG_FROM_TYPE_RCS);
                key += "-" + messageInfo.getMsgId();
                mSelectedItemList.put(key, messageInfo);
            }
        } else {
            for (ChatMsgBaseInfo value : mSelectedItemList.values()) {
                value.setChecked(isChecked);
            }
            mSelectedItemList.clear();
        }
        notifyDataSetChanged();
        if (mItemListener != null) {
            int selectedCount = mSelectedItemList.keySet().size();
            mItemListener.onSelectCountChange(selectedCount, oldSelectedCount);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int msgType = getItemViewType(position);
        convertView = mViewFactory.getMessageView(convertView, getItem(position), msgType,
                isInSelectMode, isGroupChat, isShowNickName);
        return convertView;
    }

}
