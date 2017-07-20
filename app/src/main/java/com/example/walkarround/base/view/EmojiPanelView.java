package com.example.walkarround.base.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import com.example.walkarround.R;
import com.example.walkarround.message.activity.FootPagePoint;
import com.example.walkarround.message.util.EmojiParser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 表情面板
 * Date: 2015-03-26
 *
 * @author mss
 */
public class EmojiPanelView extends RelativeLayout implements OnItemClickListener, OnPageChangeListener {

    private static final String EMOJI_ITEM_IMAGE = "ItemImage";
    private static final String EMOJI_ITEM_TEXT = "ItemText";
    public static final String EMOJI_ITEM_TYPE_DEL_BTN = "del_btn";
    /*小屏每页显示表情个数*/
    private static final int EMOJI_MAX_COUNT_PER_PAGE_S = 18;
    /*大屏每页显示表情个数*/
    private static final int EMOJI_MAX_COUNT_PER_PAGE_B = 21;

    /*当前所在页码*/
    private FootPagePoint mPagePoint;

    private EmojiListener mEmojiListener;

    public EmojiPanelView(Context context) {
        super(context);
        initPanelView();
    }

    public EmojiPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPanelView();
    }

    public EmojiPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPanelView();
    }

    private void initPanelView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infService);
        View view = inflater.inflate(R.layout.emoji_layout, this, true);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_page_vp);
        mPagePoint = (FootPagePoint) view.findViewById(R.id.page_indicator_fpp);
        initEmojiPanel(getContext(), viewPager);
    }

    /**
     * 设置点击表情监听
     *
     * @param emojiClickListener
     */
    public void setOnEmojiClickListener(EmojiListener emojiClickListener) {
        mEmojiListener = emojiClickListener;
    }

    /**
     * 初始化表情面板
     *
     * @param context
     * @param viewPager
     */
    private void initEmojiPanel(Context context, ViewPager viewPager) {
        if (viewPager == null) {
            return;
        }
        if (viewPager.getChildCount() > 0) {
            viewPager.removeAllViews();
        }

        // 计算页数
        EmojiParser parser = EmojiParser.getInstance(context.getApplicationContext());
        String[] emojiTexts = parser.getEncodedSmilyTextArray();
        int emojiTextsLen = emojiTexts.length;
        float density = context.getResources().getDisplayMetrics().density;
        int maxItemCount = density <= 1.5f ? EMOJI_MAX_COUNT_PER_PAGE_S : EMOJI_MAX_COUNT_PER_PAGE_B;
        // 每页加一个删除按钮
        maxItemCount -= 1;
        int pageCount = emojiTextsLen / maxItemCount;
        if (emojiTextsLen % maxItemCount > 0) {
            pageCount += 1;
        }
        if (pageCount == 1) {
            mPagePoint.setVisibility(View.GONE);
        } else {
            mPagePoint.setVisibility(View.VISIBLE);
            mPagePoint.resetPagePoint(pageCount, 0);
        }

        viewPager.setAdapter(new GuidePageAdapter(context, pageCount, maxItemCount, this));
        // 更新当前显示的页码位置
        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(mEmojiListener == null){
            return;
        }
        HashMap<String, Object> item = (HashMap<String, Object>) adapterView.getItemAtPosition(position);
        String itemText = "" + item.get(EMOJI_ITEM_TEXT);
        CharSequence emojiChar;
        if (EMOJI_ITEM_TYPE_DEL_BTN.equals(itemText)) {
            emojiChar = EMOJI_ITEM_TYPE_DEL_BTN;
        } else {
            emojiChar = EmojiParser.getInstance(getContext())
                    .addSmileySpans(itemText);
        }
        mEmojiListener.emojiClick(emojiChar);
    }

    private class GuidePageAdapter extends PagerAdapter {

        private Context mContext;
        private HashMap<Integer, View> mPageViewList = new HashMap<Integer, View>();
        private int mPageCount = 0;
        private int mPerPageCount = 0;
        private OnItemClickListener mItemClickListener;

        GuidePageAdapter(Context context, int pageCount, int perPageCount, OnItemClickListener itemClickListener) {
            mContext = context;
            mPageCount = pageCount < 0 ? 0 : pageCount;
            mPerPageCount = perPageCount;
            mItemClickListener = itemClickListener;
        }

        @Override
        public int getCount() {
            return mPageCount;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mPageViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mPageViewList.containsKey(position)) {
                return mPageViewList.get(position);
            }
            View pageView = createEmojiGridPages(mContext, position, mPerPageCount);
            mPageViewList.put(position, pageView);
            container.addView(pageView);
            return pageView;
        }

        /**
         * 创建一页表情
         *
         * @param context
         * @param page
         * @param perPageCount
         * @return
         */
        private View createEmojiGridPages(Context context, int page, int perPageCount) {
            EmojiParser parser = EmojiParser.getInstance(context);
            String[] emojiTexts = parser.getEncodedSmilyTextArray();
            int startPos = page * perPageCount;
            int endPos = startPos + perPageCount;
            if (endPos > emojiTexts.length) {
                endPos = emojiTexts.length;
            }
            ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
            for (int i = startPos; i < endPos; i++) {
                String emojiCode = emojiTexts[i];

                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(EMOJI_ITEM_IMAGE, parser.getSmileyDrawableId(emojiCode));
                map.put(EMOJI_ITEM_TEXT, emojiCode);// real text which is sent to server
                lstImageItem.add(map);
            }
            // 添加删除按钮
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(EMOJI_ITEM_IMAGE, R.drawable.emoji_del_btn_nor);
            map.put(EMOJI_ITEM_TEXT, EMOJI_ITEM_TYPE_DEL_BTN);
            lstImageItem.add(map);

            GridView gridview = (GridView) LayoutInflater.from(context)
                    .inflate(R.layout.im_emoji_grid_layout, null);
            SimpleAdapter saImageItems = new SimpleAdapter(context,
                    (ArrayList<HashMap<String, Object>>) lstImageItem.clone(), R.layout.im_emoji_grid_item_layout,
                    new String[]{EMOJI_ITEM_IMAGE}, new int[]{R.id.emoji_iv});
            gridview.setAdapter(saImageItems);
            gridview.setOnItemClickListener(mItemClickListener);
            return gridview;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int page) {
        mPagePoint.toPagePoint(page);
    }

    public interface EmojiListener {
        /**
         * 点击表情
         *
         * @param emojiChar
         */
        public void emojiClick(CharSequence emojiChar);
    }
}
