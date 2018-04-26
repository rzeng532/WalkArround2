package com.example.walkarround.message.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PhotoView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.listener.SearchMessageResultItemListener;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.EmojiParser;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.TimeFormattedUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchMessageResultListAdapter extends BaseAdapter {
    private static final Logger logger = Logger.getLogger(SearchMessageResultListAdapter.class.getSimpleName());
    private Context mContext;
    private LayoutInflater mInflater;
    private List<ChatMsgBaseInfo> mList = new ArrayList<ChatMsgBaseInfo>();
    private Map<String, String> mChineseKeys = new HashMap<String, String>();
    private String mKey;
    private SearchMessageResultItemListener mItemListener;
    public SearchMessageResultListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setListData(List<ChatMsgBaseInfo> list) {
        mList.clear();
        if (list != null) {
            mList.addAll(list);
        }
    }

    public void setChineseKeys(Map<String, String> map) {
        mChineseKeys.clear();
        if (map != null) {
            mChineseKeys.putAll(map);
        }

        for (Map.Entry<String, String> entry : mChineseKeys.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            logger.i("get setChineseKeys key= " + key + " value= " + value);
        }
    }

    public void clearCacheDisplayName() {
        for (ChatMsgBaseInfo sessionModel : mList) {
            sessionModel.setDisplayName(null);
        }
    }

    public void setKey(String k) {
        mKey = k;
    }

    public void setItemListener(SearchMessageResultItemListener listener) {
        this.mItemListener = listener;
    }

    @Override
    public int getCount() {
        return mList.size();
    }
    @Override
    public ChatMsgBaseInfo getItem(int position) {
        return mList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.conversation_item, null);
            holder = new ViewHolder();
            holder.ivPortrait = (PhotoView) convertView.findViewById(R.id.conv_portrait);
            holder.tvName = (TextView) convertView.findViewById(R.id.conv_name);
            holder.tvMessage = (TextView) convertView.findViewById(R.id.conv_note);
            holder.tvTime = (TextView) convertView.findViewById(R.id.conv_date);
            // holder.tvUnreadCount = (TextView) view.findViewById(R.id.conv_count);
            holder.ivTopSign = (ImageView) convertView.findViewById(R.id.conversation_item_top_sign);
            holder.ivTopSign.setVisibility(View.GONE);

            convertView.setTag(holder);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemListener != null) {
                        ViewHolder holder = (ViewHolder) view.getTag();
                        mItemListener.searchMsgOnClick(getItem(holder.position));
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;

        ChatMsgBaseInfo item = getItem(position);
        if (item.getDisplayName() == null) {
            String contact = item.getContact();
            ContactInfo info = ContactsManager.getInstance(mContext).getContactByUsrObjId(contact);
            if (info != null) {
                item.setDisplayName(info.getUsername());
                item.setProfileKey(info.getPortrait().getUrl());
            } else {
                item.setDisplayName("");
                item.setNamePinyin("");
                item.setProfileKey("");
            }
        }

        setItemPortrait(holder, item);
        setItemName(holder, item);
        setItemTime(holder, item);
        setItemMessage(holder, item);

        return convertView;
    }

    private void setItemPortrait(ViewHolder holder, ChatMsgBaseInfo item) {
        setNormalItemPortrait(holder, item);
    }

    private void setItemName(ViewHolder holder, ChatMsgBaseInfo item) {
        String displayKey = mKey;

        if (!mChineseKeys.isEmpty()) {
            String contact = item.getContact();
            String str = CommonUtils.getPhoneNum(contact);
 
            if (mChineseKeys.containsKey(str)) {
                displayKey = mChineseKeys.get(str);
            }
        }

        /* 默认文字少于1行全部显示 */
        if (!TextUtils.isEmpty(item.getDisplayName())) {
            if (TextUtils.isEmpty(displayKey)) {
                holder.tvName.setText(item.getDisplayName());
            } else {
                holder.tvName.setText(Html.fromHtml(item.getDisplayName().replace(displayKey, "<font color='#0089DD'>" + displayKey + "</font>")));
            }
        } else {
            String displayStr = item.getContact();
            if (!TextUtils.isEmpty(displayKey) && !TextUtils.isEmpty(displayStr)) {
                holder.tvName.setText(Html.fromHtml(item.getContact().replace(displayKey, "<font color='#0089DD'>" + displayKey + "</font>")));
            } else {
                holder.tvName.setText(displayStr);
            }
        }
    }

    private void setItemTime(ViewHolder holder, ChatMsgBaseInfo item) {
        holder.tvTime.setText(TimeFormattedUtil.getListDisplayTime(mContext, item.getTime()));
    }

    // 文字少于1行(假设1行19个字符)全部显示；文字多余1行，关键字靠左，靠右，居中(前面10个后面9个字符的位置)的显示，
    private void setItemMessage(ViewHolder holder, ChatMsgBaseInfo item) {
        if (TextUtils.isEmpty(item.getData())) {
            holder.tvMessage.setText("");
            return;
        }
        String text = item.getData();
        int i = text.indexOf(mKey);
        if (i < 0) {
            holder.tvMessage.setText(EmojiParser.getInstance(mContext).addSmileySpans(text));
            return;
        }

        /* 需要判断越界 */
        String subText;
        if (text.length() < 19) {// 少于1行
            subText = text;
        } else {
            if (i >= 0 && i + 9 > text.length()) {
                subText = "..." + text.substring(text.length() - 19);// 关键字靠右
            } else {
                if (i - 10 > 0) {
                    subText = "..." + text.substring(i - 10);// 关键字居中
                } else {
                    subText = text;// 关键字靠左
                }
            }
        }
        holder.tvMessage.setText(EmojiParser.getInstance(mContext).addSmileySpans(subText, mKey, mContext.getResources().getColor(R.color.fontcor6)));
    }

    private void setNormalItemPortrait(ViewHolder holder, ChatMsgBaseInfo item) {
        if (!TextUtils.isEmpty(item.getDisplayName())) {
            holder.ivPortrait.setBaseData(item.getDisplayName(), item.getProfileKey(), item.getNamePinyin(),
                    R.drawable.default_profile_portrait);
        } else {
            // 空时给个默认
            holder.ivPortrait.setBaseData(null, null, null, R.drawable.default_profile_portrait);
        }
    }

    public class ViewHolder {
        int position;
        // view
        PhotoView ivPortrait;

        ImageView ivTopSign;
        TextView tvName;
        TextView tvMessage;
        TextView tvTime;
        // TextView tvUnreadCount;
    }
}
