package com.example.walkarround.message.adapter;

import android.app.NotificationManager;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PhotoView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.listener.ConversationItemListener;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.EmojiParser;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageConstant.ChatType;
import com.example.walkarround.message.util.MessageConstant.ConversationType;
import com.example.walkarround.message.util.MessageConstant.TopState;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.SessionComparator;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.TimeFormattedUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 消息List数据展示
 */
public class BaseConversationListAdapter extends BaseAdapter implements OnClickListener, OnLongClickListener {

    private static final Logger logger = Logger.getLogger(BaseConversationListAdapter.class.getSimpleName());
    // 是否批量操作模式
    private boolean mIsBatchOperation;
    protected Context mContext;
    private LayoutInflater mInflater;
    // 显示内容数据
    private List<MessageSessionBaseModel> mListData = new ArrayList<MessageSessionBaseModel>();
    // 选中的项目位置
    private HashMap<Long, MessageSessionBaseModel> mChosenPositionList = new HashMap<Long, MessageSessionBaseModel>();
    // 点击事件监听
    private ConversationItemListener mItemListener;

    public BaseConversationListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    /**
     * 设置数据
     *
     * @param list
     */
    public void setListData(List<MessageSessionBaseModel> list) {
        mListData.clear();
        if (list == null || list.size() == 0) {
            return;
        }
        mListData.addAll(list);
    }

    public void addListData(List<MessageSessionBaseModel> list) {
        if (list != null && list.size() > 0) {

        }
        mListData.addAll(list);
    }

    public void addListData(MessageSessionBaseModel item) {
        if (item == null) {
            return;
        }
        mListData.add(item);
    }

    public void removeListData(MessageSessionBaseModel item) {
        if (item == null) {
            return;
        }
        mListData.remove(item);
    }


    /**
     * 重新排序
     *
     * @param sortOrder
     */
    public void sortListData(int sortOrder) {
        Collections.sort(mListData, new SessionComparator(sortOrder));
    }

    /**
     * 设置是否批量操作模式
     *
     * @param isBatchOperate
     */
    public void setBatchOperation(boolean isBatchOperate) {
        if (isBatchOperate == mIsBatchOperation) {
            return;
        }
        mIsBatchOperation = isBatchOperate;
        clearAllChosenPosition();
    }

    /**
     * 是否批量操作模式
     *
     * @return
     */
    public boolean getBatchOperation() {
        return mIsBatchOperation;
    }

    public void clearCacheDisplayName() {
        for (MessageSessionBaseModel sessionModel : mListData) {
            if (sessionModel.getItemType() == ConversationType.GENERAL) {
                sessionModel.name = null;
            }
        }
    }

    /**
     * 设置点击事件监听
     *
     * @param listener
     */
    public void setItemListener(ConversationItemListener listener) {
        this.mItemListener = listener;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public MessageSessionBaseModel getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        MessageSessionBaseModel listDO = mListData.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.conversation_item, null);
            holder = new ViewHolder();
            holder.rlConversation = (RelativeLayout) convertView.findViewById(R.id.conv_rl);
            holder.ivPortrait = (PhotoView) convertView.findViewById(R.id.conv_portrait);
            holder.ivDelIcon = (ImageView) convertView.findViewById(R.id.conversation_item_del_icon);
            //holder.ivPortrait.setCheckBoxResId(R.drawable.public_icon_list_checkbox_on,
            //        R.drawable.public_icon_list_checkbox_off);
            //holder.ivPortrait.setCheckBoxClickable(false);
            holder.tvName = (TextView) convertView.findViewById(R.id.conv_name);
            holder.tvMessage = (TextView) convertView.findViewById(R.id.conv_note);
            holder.tvTime = (TextView) convertView.findViewById(R.id.conv_date);
            holder.tvUnreadCount = (TextView) convertView.findViewById(R.id.conv_count);
            holder.tvMappingFlag = (TextView) convertView.findViewById(R.id.conv_mapping_flag);
            holder.ivTopSign = (ImageView) convertView.findViewById(R.id.conversation_item_top_sign);

            holder.ivDelIcon.setTag(holder);
            holder.ivDelIcon.setOnClickListener(this);

            holder.rlConversation.setTag(holder);
            holder.rlConversation.setOnClickListener(this);

            convertView.setTag(holder);
//            convertView.setOnClickListener(this);
            //Disable long click here.
            //convertView.setOnLongClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        initViewHolder(holder, listDO, position, (position <= 0 ? true : mListData.get(position - 1).status < MessageUtil.WalkArroundState.STATE_END));
        return convertView;
    }

    /**
     *
     * @param holder
     * @param listDO
     * @param position
     * @param isThereMappingConv， 判断上一个item是否是mapping 关系
     */
    private void initViewHolder(ViewHolder holder, MessageSessionBaseModel listDO, int position, boolean isThereMappingConv) {
        setItemContactInfo(holder, listDO);
        setItemTime(holder, listDO);
        setItemTop(holder, listDO);
        setItemRead(holder, listDO);
        setItemMessage(holder, listDO);
        setItemFlag(holder, listDO, position, isThereMappingConv);
    }

    /**
     * 初始化联系人信息
     *
     * @param holder
     * @param listDO
     */
    private void setItemContactInfo(ViewHolder holder, MessageSessionBaseModel listDO) {

        ContactInfo info = null;

        if (listDO.name == null) {
            // 首次，则获取联系人信息
            info = ContactsManager.getInstance(mContext).getContactByUsrObjId(listDO.getContact());
            //TODO: get contact infor by user object id.
                //ContactInfo info = NewContactManager.getInstance(mContext)
                //        .getDetailByPhoneNumber(listDO.getContact());
                if (info != null) {
                    listDO.name = info.getUsername();
                    listDO.profile = info.getPortrait().getUrl();
                    listDO.defaultResId = R.drawable.contact_default_profile;
                } else {
                    listDO.profile = null;
                    listDO.name = "";
                    listDO.defaultResId = R.drawable.contact_default_profile;
                }
        }

        String name;
        if (!TextUtils.isEmpty(listDO.name)) {
            name = listDO.name;
        } else if(info != null && !TextUtils.isEmpty(info.getMobilePhoneNumber())) {
            name = info.getMobilePhoneNumber();
        } else {
            name = listDO.getContact();
        }
        holder.tvName.setText(name);

        //Set photo
        holder.ivPortrait.setCheckBoxVisibility(View.GONE);
        holder.ivPortrait.setChecked(false);
        holder.ivPortrait.setBaseData(name, listDO.profile, listDO.nameLastC, listDO.defaultResId);
    }

    /**
     * 获取联系人姓名
     *
     * @param number
     * @return
     */
    private String getContactName(String number) {
        ContactInfo info = ContactsManager.getInstance(mContext).getContactByUsrObjId(number);
        String name = "";
        if (info != null) {
            name = info.getUsername();
        } else {
            name = number;
        }

        return name;
    }

    /**
     * 设置消息显示时间
     * @param holder
     * @param listDO
     */
    private void setItemTime(ViewHolder holder, MessageSessionBaseModel listDO) {
        if (listDO.getItemType() == ConversationType.PUBLIC_ACCOUNT
                || listDO.getItemType() == ConversationType.SYSTEM) {
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(TimeFormattedUtil.getListDisplayTime(mContext, listDO.getLastTime()));
        }
    }

    /**
     * 置顶消息标记
     *
     * @param holder
     * @param listDO
     */
    private void setItemTop(ViewHolder holder, MessageSessionBaseModel listDO) {
        //We don't need UI flag now.
//        if (listDO.getTop() == TopState.TOP) {
//            holder.ivTopSign.setVisibility(View.VISIBLE);
//        } else {
//            holder.ivTopSign.setVisibility(View.GONE);
//        }
    }

    private void setItemRead(ViewHolder holder, MessageSessionBaseModel listDO) {
        if (listDO.unReadCount > 0) {
            holder.tvUnreadCount.setText(MessageUtil.getDisplayUnreadCount(listDO.unReadCount));
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadCount.setText("");
            holder.tvUnreadCount.setVisibility(View.GONE);
        }
    }

    private void setItemMessage(ViewHolder holder, MessageSessionBaseModel listDO) {
        if (listDO.msgStatus == MessageConstant.MessageState.MSG_STATE_SEND_DRAFT
                && !TextUtils.isEmpty(listDO.getData())) {
            // 草稿消息
            String draft = mContext.getString(R.string.draft);
            SpannableStringBuilder builder = (SpannableStringBuilder) EmojiParser.getInstance(mContext)
                    .addSmileySpans(draft + listDO.getData(), 0.4f);
            int color = mContext.getResources().getColor(R.color.cor_red);
            builder.setSpan(new ForegroundColorSpan(color), 0, draft.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvMessage.setText(builder);
            holder.tvMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            return;
        }
        String displayStr = "";
        if (listDO.isBurnAfterMsg()) {
            //displayStr = mContext.getString(R.string.msg_session_burn_after);
        } else {
            switch (listDO.getContentType()) {
                case MessageConstant.MessageType.MSG_TYPE_TEXT:
                    displayStr = listDO.getData();
                    break;
                case MessageConstant.MessageType.MSG_TYPE_AUDIO:
                    displayStr = mContext.getString(R.string.msg_session_audio);
                    break;
                case MessageConstant.MessageType.MSG_TYPE_VIDEO:
                    displayStr = mContext.getString(R.string.msg_session_video);
                    break;
                case MessageConstant.MessageType.MSG_TYPE_IMAGE:
                    displayStr = mContext.getString(R.string.msg_session_picture);
                    break;
                case MessageConstant.MessageType.MSG_TYPE_MAP:
                    displayStr = mContext.getString(R.string.msg_session_location);
                    break;
                case MessageConstant.MessageType.MSG_TYPE_NOTIFICATION:
                    displayStr = mContext.getString(R.string.msg_session_sys_msg);
                    break;
                default:
                    displayStr = listDO.getData();
                    break;
            }
        }
        switch (listDO.msgStatus) {
            case MessageConstant.MessageState.MSG_STATE_SEND_ING:
                holder.tvMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.message_icon_list_sending, 0, 0, 0);
                break;
            case MessageConstant.MessageState.MSG_STATE_SEND_FAIL:
                holder.tvMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.message_icon_list_error, 0, 0, 0);
                break;
            default:
                holder.tvMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                break;
        }
        holder.tvMessage.setText(EmojiParser.getInstance(mContext).addSmileySpans(displayStr, 0.4f));
    }

    public void setItemFlag(ViewHolder holder, MessageSessionBaseModel listDO, int position, boolean priorIsMappingConv) {
        //Init flags
        int convState = listDO.status;
        logger.d("color index is: " + listDO.colorIndex);
        logger.d("color is: " + MessageUtil.getFriendColor(listDO.colorIndex));

        //Invalide value, just return.
        logger.d("color is: " + convState);
        if(convState == -1) {
            return;
        }

        if(convState <  MessageUtil.WalkArroundState.STATE_END && convState >= MessageUtil.WalkArroundState.STATE_IM) {
            holder.tvMappingFlag.setVisibility(View.VISIBLE);
            holder.tvMappingFlag.setText(R.string.msg_conversation_mapping);
            holder.ivDelIcon.setVisibility(View.VISIBLE);
            //Set correct text font color for this case.
            holder.tvMessage.setTextColor(mContext.getResources().getColor(R.color.fontcor1));
            holder.rlConversation.setBackground(mContext.getResources().getDrawable(R.drawable.list_item_bg));
        } else if(convState ==  MessageUtil.WalkArroundState.STATE_END) {
            if(position <= 1 && priorIsMappingConv) {
                holder.tvMappingFlag.setVisibility(View.VISIBLE);
                holder.tvMappingFlag.setText(R.string.msg_conversation_walking_friends);
            } else {
                holder.tvMappingFlag.setVisibility(View.GONE);
            }
            holder.ivDelIcon.setVisibility(View.GONE);
            holder.rlConversation.setBackgroundColor(mContext.getResources().getColor(MessageUtil.getFriendColor(listDO.colorIndex)));
        } else {
            if(position >= 1) {
                MessageSessionBaseModel priorModel = mListData.get(position - 1);
                if(priorModel != null && priorModel.status == MessageUtil.WalkArroundState.STATE_INIT) {
                    holder.tvMappingFlag.setVisibility(View.GONE);
                } else {
                    holder.tvMappingFlag.setVisibility(View.VISIBLE);
                    holder.tvMappingFlag.setText(R.string.msg_conversation_unkown_friends);
                }
            } else if (position == 0) {
                holder.tvMappingFlag.setVisibility(View.VISIBLE);
                holder.tvMappingFlag.setText(R.string.msg_conversation_unkown_friends);
            }

            holder.tvMessage.setTextColor(mContext.getResources().getColor(R.color.fontcor1));
            holder.rlConversation.setBackgroundColor(mContext.getResources().getColor(R.color.bgcor14));
            //holder.rlConversation.setBackground(mContext.getResources().getDrawable(R.drawable.list_item_bg));
            holder.ivDelIcon.setVisibility(View.GONE);
        }
    }

    /*  置顶/消顶功能是否可用 */
    public boolean shouldEnableTop() {
        if (mChosenPositionList == null || mChosenPositionList.isEmpty()) {
            return false;
        }
        int notTopCount = 0;
        int topCount = 0;
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            MessageSessionBaseModel item = value;
            if (/* !isTopThreadId(item.getThreadId()) */item.getTop() == TopState.NOT_TOP) {
                notTopCount++;
            } else {
                topCount++;
            }
        }
        if (notTopCount == mChosenPositionList.size() || topCount == mChosenPositionList.size()) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {

        if (mItemListener != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            MessageSessionBaseModel item = getItem(holder.position);

            if(view != null && view.getId() == R.id.conversation_item_del_icon) {
                mItemListener.onDeleteConversationItem(getItem(holder.position));
                return;
            } else if(view != null && view.getId() == R.id.conv_rl) {
                mItemListener.conversationItemOnClick(getItem(holder.position));
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        mIsBatchOperation = !mIsBatchOperation;
        ViewHolder holder = (ViewHolder) view.getTag();
        if (mIsBatchOperation) {
            MessageSessionBaseModel model = getItem(holder.position);
            if (canSelectable(model)) {
                mChosenPositionList.put(model.getThreadId(), getItem(holder.position));
            }
        } else {
            clearAllChosenPosition();
            notifyDataSetChanged();
        }
        if (mItemListener != null) {
            mItemListener.onSelectModeChanged(mIsBatchOperation);
        }
        return true;
    }

    public void put2ChoosenList(MessageSessionBaseModel item) {
        if(item == null) {
            return;
        }

        if (canSelectable(item)) {
            mChosenPositionList.put(item.getThreadId(), item);
        }
    }

    public class ViewHolder {
        public int position;
        public PhotoView ivPortrait;
        RelativeLayout rlConversation;
        ImageView ivTopSign;
        ImageView ivDelIcon;
        TextView tvName;
        TextView tvMessage;
        TextView tvTime;
        TextView tvUnreadCount;
        TextView tvMappingFlag;
    }

    private boolean isChosenPosition(long threadId) {
        return mChosenPositionList.containsKey(threadId);
    }

    /**
     * 选中项目个数
     *
     * @return 项目个数
     */
    public int getChosenItemCount() {
        return mChosenPositionList.size();
    }

    /**
     * @return
     * @方法名：isGroupSessionChosen
     * @描述：判断是否选中群聊或者群发
     * @输出：boolean
     * @作者：sjf
     */
    public boolean isGroupSessionChosen() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            int chatType = value.getChatType();
            if (chatType == ChatType.CHAT_TYPE_GROUP) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否可选中
     * @return
     */
    protected boolean canSelectable(MessageSessionBaseModel item) {
        return true;
    }

    /**
     * 项目设置为选中
     */
    public void addToChosenPositionList(int position) {
        MessageSessionBaseModel item = getItem(position);
        if (!mChosenPositionList.containsKey(item.getThreadId())) {
            mChosenPositionList.put(item.getThreadId(), item);
        }
    }

    /**
     * 选中项目设置为非选中
     */
    public void removeFromChosenPositionList(long threadId) {
        mChosenPositionList.remove(threadId);
    }

    /**
     * 撤销所有选中
     */
    private void clearAllChosenPosition() {
        mChosenPositionList.clear();
    }

    /**
     * 是否全选中了
     *
     * @return
     */
    public boolean isSelectAll() {
        return mChosenPositionList.size() == mListData.size();
    }

    /**
     * 全选
     */
    public void setSelectAll() {
        for (int i = 0; i < mListData.size(); i++) {
            addToChosenPositionList(i);
        }
    }

    /**
     * 取消全选
     */
    public void setAllUnchecked() {
        mChosenPositionList.clear();
    }

    /**
     * 删除选择的项目
     */
    public void deleteSelectedItem() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            mListData.remove(value);
        }
        mChosenPositionList.clear();
    }

    /**
     * 删除已经删除的会话
     */
    public void deleteSelectedDeletedItem() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            if (value.getThreadId() > 0) {
                continue;
            }
            mListData.remove(value);
        }
        mChosenPositionList.clear();
    }

    /**
     * 设置已选的项目为已读
     */
    public void setSelectedItemRead() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            value.isUnread = false;
            value.unReadCount = 0;
            cancelNotification(value);
        }
        mChosenPositionList.clear();
    }

    /**
     * 取消通知消息
     */
    private void cancelNotification(MessageSessionBaseModel conversation) {
        int conversationType = conversation.getChatType();
        if (conversationType == ChatType.CHAT_TYPE_ONE2ONE) {
            try {
                String number = CommonUtils.getPhoneNum(conversation.getContact());
                int startPos = number.length() > 5 ? number.length() - 5 : 0;
                int id = Integer.parseInt(number.substring(startPos));
                NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(number, id);
            } catch (NumberFormatException e) {
            }
        }
    }

    /* 根据已选中的项目来判断置顶是否置已读 */
    public boolean shouldSetRead() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            MessageSessionBaseModel item = value;
            if (item.unReadCount > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 置顶项目是否可用
     *
     * @return
     */
    public boolean shouldTopMessage() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            MessageSessionBaseModel item = value;
            if (item.getTop() != TopState.NOT_TOP) {
                return false;
            }
        }
        return true;
    }

    /**
     * 设置所有已选项目置顶
     */
    public void setChoseItemTop() {
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            value.setTop(TopState.TOP);
        }
    }

    /**
     * 设置所有已选项目置顶
     */
    public void updateItemData(MessageSessionBaseModel item, MessageSessionBaseModel newValue) {
        if (item == null || newValue == null) {
            return;
        }
        item.setContentType(newValue.getContentType());
        item.setData(newValue.getData());
        item.setLastTime(newValue.getLastTime());
        item.setTime(newValue.getTime());
        item.setTop(newValue.getTop());
        item.setIsBurnAfterMsg(newValue.isBurnAfterMsg());
        item.setSendReceive(newValue.getSendReceive());
        item.unReadCount = newValue.unReadCount;
        item.msgStatus = newValue.msgStatus;
        item.msgId = newValue.msgId;
        item.isUnread = newValue.isUnread;
        item.name = null;
    }

    /**
     * 设置所有已选项目置顶
     */
    public MessageSessionBaseModel findItemData(long threadId) {
        if (threadId <= 0) {
            return null;
        }
        for (MessageSessionBaseModel item : mListData) {
            if (threadId == item.getThreadId()) {
                return item;
            }
        }
        return null;
    }

    /**
     * 删除对应id的会话
     *
     * @param threadId
     */
    public void deleteItemData(long threadId) {
        if (threadId <= 0) {
            return;
        }
        MessageSessionBaseModel targetItem = null;
        for (MessageSessionBaseModel item : mListData) {
            if (threadId == item.getThreadId()) {
                targetItem = item;
                break;
            }
        }
        if (targetItem != null) {
            mListData.remove(targetItem);
            notifyDataSetChanged();
        }
    }

    /**
     * 删除对应id的会话
     *
     * @param item
     */
    public void deleteItemData(MessageSessionBaseModel item) {
        if (item != null) {
            mListData.remove(item);
        }
    }

    /**
     * 获取所有选中的项目
     *
     * @param shoudcreate 是否需要拷贝
     * @return
     */
    public List<MessageSessionBaseModel> getChosenItems(boolean shoudcreate) {
        List<MessageSessionBaseModel> chosenList = new ArrayList<MessageSessionBaseModel>();
        for (MessageSessionBaseModel value : mChosenPositionList.values()) {
            if (value.getThreadId() == -2) {
                sdkGetMsgThreadId(value, shoudcreate);
            }
            chosenList.add(value);
        }
        return chosenList;
    }

    /**
     * 获取选中的item中会话ID为threadId的项
     *
     * @param threadId 会话ID
     * @return
     */
    public MessageSessionBaseModel getChosenItems(long threadId) {
        return mChosenPositionList.get(threadId);
    }

    /**
     * 所有选中项目的key(位置)
     *
     * @return
     */
    protected List<Long> getChosenItemsThreadId() {
        List<Long> chosenList = new ArrayList<Long>();
        chosenList.addAll(mChosenPositionList.keySet());
        return chosenList;
    }

    private void sdkGetMsgThreadId(MessageSessionBaseModel item, boolean shouldCreate) {
        long id;
        List<String> numbers = new ArrayList<String>();
        numbers.add(item.getContact());
        if (shouldCreate) {
            id = WalkArroundMsgManager.getInstance(mContext).createConversationId(item.getChatType(), numbers);
            item.setThreadId(id);
        } else {
            id = WalkArroundMsgManager.getInstance(mContext).getConversationId(item.getChatType(), numbers);
            if (id != 0) {
                item.setThreadId(id);
            }
        }
    }

}
