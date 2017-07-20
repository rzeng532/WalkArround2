package com.example.walkarround.message.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.example.walkarround.R;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatAssistToolsView extends LinearLayout implements View.OnClickListener, OnItemClickListener {

    private static final Logger logger = Logger.getLogger(ChatAssistToolsView.class.getSimpleName());
    private static final int COLUMN_COUNT = 4;
    private final static int TOOLS_PER_PAGE_COUNT = 8;
    private static final int[] TOOLS_TEXT_RES_IDS = {R.string.chatting_picture, R.string.chatting_location};
    private static final int[] TOOLS_DRAWABLE_RES_IDS = {R.drawable.chat_picture_bg, R.drawable.chat_location_bg};

    private ToolsViewOnClick mToolItemClickListener;
    private ListView mPhrasebookListView;

    private FootScrollLayout mToolsPanelView;
    private HashMap<Integer, ToolsInfo> mToolsViewInfo = new HashMap<Integer, ToolsInfo>();

    public ChatAssistToolsView(Context context) {
        super(context);
        initView();
        logger.d("constructor 1");
    }

    public ChatAssistToolsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        logger.d("constructor 2");
    }

    public ChatAssistToolsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
        logger.d("constructor 3");
    }

    private void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infService);
        View view = inflater.inflate(R.layout.chat_assist_tools, this, true);
        for (int i = 0; i < TOOLS_TEXT_RES_IDS.length; i++) {
            ToolsInfo toolsInfo = new ToolsInfo();
            toolsInfo.drawableResId = TOOLS_DRAWABLE_RES_IDS[i];
            toolsInfo.isEnable = true;
            toolsInfo.visibility = VISIBLE;
            int toolsTextId = TOOLS_TEXT_RES_IDS[i];
            toolsInfo.textResId = toolsTextId;
            mToolsViewInfo.put(toolsTextId, toolsInfo);
        }

        mToolsPanelView = (FootScrollLayout) view.findViewById(R.id.chat_tool_panel_sl);
        FootPagePoint pp = (FootPagePoint) view.findViewById(R.id.chat_tool_page_pp);
        mToolsPanelView.setPagePoint(pp);
        updateToolsView();
    }

    /**
     * 更新菜单的显示
     */
    private void updateToolsView() {
        FootPagePoint pagePoint = mToolsPanelView.getPagePoint();
        mToolsPanelView.removeAllViews();
        ArrayList<ToolsInfo> visibleTools = new ArrayList<ToolsInfo>();
        for (int toolKey : TOOLS_TEXT_RES_IDS) {
            if (mToolsViewInfo.containsKey(toolKey)
                    && mToolsViewInfo.get(toolKey).visibility == VISIBLE) {
                visibleTools.add(mToolsViewInfo.get(toolKey));
            }
        }

        Context context = getContext();
        int pageCount = (int) Math.ceil(visibleTools.size() / (float) TOOLS_PER_PAGE_COUNT);
        if (pageCount == 1) {
            // 只有一页
//            int columns = visibleTools.size() < COLUMN_COUNT ?
//                    visibleTools.size() : COLUMN_COUNT;
            GridView gv = createToolsPageView(context, COLUMN_COUNT);
            ToolsViewAdapter adapter = new ToolsViewAdapter(context);
            adapter.setResInfo(visibleTools);
            adapter.setViewClickListener(this);
            gv.setAdapter(adapter);
            mToolsPanelView.addView(gv);
            pagePoint.setVisibility(View.INVISIBLE);
        } else {
            for (int i = 0; i < pageCount; i++) {
                GridView gv = createToolsPageView(context, COLUMN_COUNT);
                int startPos = i * TOOLS_PER_PAGE_COUNT;
                int endPos = startPos + TOOLS_PER_PAGE_COUNT;
                endPos = endPos > visibleTools.size() ? visibleTools.size() : endPos;
                List<ToolsInfo> pageTools = visibleTools.subList(startPos, endPos);
                ToolsViewAdapter adapter = new ToolsViewAdapter(context);
                adapter.setResInfo(pageTools);
                adapter.setViewClickListener(this);
                gv.setAdapter(adapter);
                mToolsPanelView.addView(gv);
            }
            pagePoint.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 创建一页平均布局的GridView
     *
     * @param context
     * @return
     */
    private GridView createToolsPageView(Context context, int columns) {
        GridView gridView = new GridView(context);
        gridView.setFocusable(false);
        gridView.setVerticalSpacing(CommonUtils.dip2px(context, 5));
        gridView.setHorizontalSpacing(CommonUtils.dip2px(context, 5));
        gridView.setNumColumns(columns);
        return gridView;
    }

    /**
     * @param toolItemClickListener
     * @方法名：setOnClickListener
     * @描述：设置点击事件监听
     * @输出：void
     * @作者：
     */
    public void setToolsOnClickListener(ToolsViewOnClick toolItemClickListener) {
        mToolItemClickListener = toolItemClickListener;
    }

    public void setPictureEnable(boolean enabled) {
        mToolsViewInfo.get(R.string.chatting_picture).isEnable = enabled;
        updateToolsView();
    }

//    public void setVideoEnable(boolean enabled) {
//        mToolsViewInfo.get(R.string.chatting_video).isEnable = enabled;
//        updateToolsView();
//    }

    public void setLocationEnable(boolean enabled) {
        mToolsViewInfo.get(R.string.chatting_location).isEnable = enabled;
        updateToolsView();
    }

    public void setPictureVisibility(int visibility) {
        mToolsViewInfo.get(R.string.chatting_picture).visibility = visibility;
        updateToolsView();
    }

//    public void setVideoVisibility(int visibility) {
//        mToolsViewInfo.get(R.string.chatting_video).visibility = visibility;
//        updateToolsView();
//    }

    public void setLocationVisibility(int visibility) {
        mToolsViewInfo.get(R.string.chatting_location).visibility = visibility;
        updateToolsView();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE && mPhrasebookListView != null
                && mPhrasebookListView.getVisibility() == VISIBLE) {
            mPhrasebookListView.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_tool_tv:
                int textResId = (Integer) view.getTag(R.id.chat_tool_tv);
                toolsViewClick(textResId);
                break;
            default:
                break;
        }
    }

    /**
     * 根据显示字符串确认点击的项目
     *
     * @param textResId
     */
    private void toolsViewClick(int textResId) {
        switch (textResId) {
//            case R.string.chatting_contacts:
//                if (mToolItemClickListener != null) {
//                    mToolItemClickListener.onContactsClick();
//                }
//                break;
            case R.string.chatting_picture:
                if (mToolItemClickListener != null) {
                    mToolItemClickListener.onPictureClick();
                }
                break;
//            case R.string.chatting_video:
//                if (mToolItemClickListener != null) {
//                    mToolItemClickListener.onVideoClick();
//                }
//                break;
            case R.string.chatting_location:
                if (mToolItemClickListener != null) {
                    mToolItemClickListener.onLocation();
                }
                break;
//            case R.string.chatting_timesend:
//                if (mToolItemClickListener != null) {
//                    mToolItemClickListener.onSetTimeOnClick();
//                }
//                break;
//            case R.string.chatting_fire:
//                if (mToolItemClickListener != null) {
//                    mToolItemClickListener.onReadBurnClick();
//                }
//                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long arg3) {
        // 点击常用语
        listView.setVisibility(GONE);
        if (mToolItemClickListener != null) {
            mToolItemClickListener.onPhrasebookSelected((String) view.getTag());
        }
    }

    /**
     * 常用语
     */
    private static class PhrasebookListAdapter extends BaseAdapter {
        private String[] mListData;
        private LayoutInflater mInflater;

        public PhrasebookListAdapter(Context context, String[] data) {
            this.mListData = data;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mListData.length;
        }

        @Override
        public String getItem(int position) {
            return mListData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.phrasehbook_list_item, null);
            }
            TextView itemText = (TextView) convertView;
            itemText.setTag(getItem(position));
            itemText.setText(getItem(position));
            return convertView;
        }
    }

    /**
     * 菜单按钮
     */
    public class ToolsViewAdapter extends BaseAdapter {

        private ArrayList<ToolsInfo> mToolsInfoList = new ArrayList<ToolsInfo>();
        private LayoutInflater mInflater;
        private OnClickListener mViewClickListener;

        public ToolsViewAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        /**
         * 设置所有文字和图片资源
         *
         * @param toolsInfoList
         */
        public void setResInfo(List<ToolsInfo> toolsInfoList) {
            mToolsInfoList.clear();
            mToolsInfoList.addAll(toolsInfoList);
        }

        public void setViewClickListener(OnClickListener viewClickListener) {
            mViewClickListener = viewClickListener;
        }

        @Override
        public int getCount() {
            return mToolsInfoList.size();
        }

        @Override
        public ToolsInfo getItem(int position) {
            return mToolsInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView toolsTv;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.chat_tools_item_view, null);
                convertView.setLayoutParams(new GridView.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                toolsTv = (TextView) convertView.findViewById(R.id.chat_tool_tv);
                convertView.setTag(toolsTv);
                convertView.setOnClickListener(mViewClickListener);
            } else {
                toolsTv = (TextView) convertView.getTag();
            }
            ToolsInfo toolsInfo = getItem(position);
            convertView.setTag(R.id.chat_tool_tv, toolsInfo.textResId);
            toolsTv.setText(toolsInfo.textResId);
            toolsTv.setCompoundDrawablesWithIntrinsicBounds(0, toolsInfo.drawableResId, 0, 0);
            toolsTv.setEnabled(toolsInfo.isEnable);
            return convertView;
        }
    }

    private class ToolsInfo {
        int textResId;
        int drawableResId;
        boolean isEnable;
        int visibility;
    }

    public interface ToolsViewOnClick {
        /**
         * @param selectedStr
         * @方法名：onPhrasebookSelected
         * @描述：选择常用语后
         * @输出：void
         * @作者：mss
         */
        public void onPhrasebookSelected(String selectedStr);

        /**
         * @方法名：onContactsClick
         * @描述：点击了联系人
         * @输出：void
         * @作者：mss
         */
        //public void onContactsClick();

        /**
         * @方法名：onPictureClick
         * @描述：点击了图片
         * @输出：void
         * @作者：mss
         */
        public void onPictureClick();

        /**
         * @方法名：onVideoClick
         * @描述：点击了视频
         * @输出：void
         * @作者：mss
         */
        public void onVideoClick();

        /**
         * @方法名：onLocation
         * @描述：点击了位置
         * @输出：void
         * @作者：mss
         */
        public void onLocation();

        /**
         * @方法名：onTimeSendClick
         * @描述：点击了定时发送
         * @输出：void
         * @作者：mss
         */
        //public void onSetTimeOnClick();

        /**
         * @方法名：onReadBurnClick
         * @描述：点击了阅后即焚
         * @输出：void
         * @作者：mss
         */
        //public void onReadBurnClick();
    }

}
