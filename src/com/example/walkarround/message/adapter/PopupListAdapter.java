package com.example.walkarround.message.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.walkarround.R;

import java.util.ArrayList;
import java.util.List;

/**
 * PopupWidow的展示项目
 * Date: 2015-03-25
 *
 * @author mss
 */
public class PopupListAdapter extends BaseAdapter implements View.OnClickListener {

    /* 消息头部电话Popup */
    public static final int TYPE_MESSAGE_CALL = 1;
    /* 消息头部更多Popup */
    public static final int TYPE_MESSAGE_MORE = 2;
    private Context mContext;
    /* 列表选项的内容 */
    private List<String> mDisplayStrList = new ArrayList<String>();
    private PopupListItemListener mItemListener;
    /* Popup View的类型 */
    private int mPopupType = 0;

    public PopupListAdapter(Context context, PopupListItemListener itemListener) {
        mContext = context;
        mItemListener = itemListener;
    }

    /**
     * 设置显示选项内容
     *
     * @param popupType      类型
     * @param displayStrList 选项内容
     */
    public void setDisplayStrList(int popupType, List<String> displayStrList) {
        mPopupType = popupType;
        mDisplayStrList.clear();
        if (displayStrList != null) {
            mDisplayStrList.addAll(displayStrList);
        }
    }

    /**
     * 当前的Popup类型
     *
     * @return 当前的Popup类型
     */
    public int getPopupType() {
        return mPopupType;
    }

    @Override
    public int getCount() {
        return mDisplayStrList.size();
    }

    @Override
    public String getItem(int position) {
        return mDisplayStrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.popup_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.dividerView = convertView.findViewById(R.id.divider_v);
            viewHolder.titleView = (TextView) convertView.findViewById(R.id.popup_list_title_tv);
            convertView.setTag(viewHolder);
            convertView.setOnClickListener(this);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.position = position;
        viewHolder.titleView.setText(getItem(position));
        if (position == getCount() - 1) {
            viewHolder.dividerView.setVisibility(View.GONE);
        } else {
            viewHolder.dividerView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.popup_list_item_ll:
                if (mItemListener == null) {
                    return;
                }
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                mItemListener.popupListItemOnClick(mPopupType, viewHolder.position);
                break;
            default:
                break;
        }
    }

    class ViewHolder {
        int position;
        TextView titleView;
        View dividerView;
    }

    public interface PopupListItemListener {
        /**
         * 点击Item
         *
         * @param type     Popup View的类型
         * @param position 点击的位置
         */
        public void popupListItemOnClick(int type, int position);
    }
}
