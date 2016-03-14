package com.example.walkarround.base.view;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.walkarround.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 循环选择滚轮
 * Date: 2015-03-03
 *
 * @author mss
 */
public class NumericPickerWheelView extends LinearLayout implements AbsListView.OnScrollListener {

    /*默认显示项目行数*/
    private final int DEFAULT_ROW_COUNT = 3;
    /*显示最大和最小字号*/
    private int mMaxTextSize = 32;
    private int mMinTextSize = 28;
    /*展示要选择的内容*/
    private ListView mNumericListView;
    private NumericPickerAdapter mNumericAdapter;
    /*滚动停止即选中后事件处理*/
    private onSelectListener mSelectListener;

    /*最后一次选中的Value*/
    private String mLastSelectedItem = null;
    /*显示行数*/
    private int mDisplayRowCount = DEFAULT_ROW_COUNT;

    /*选择项目文字色*/
    private int mSelectedTextColor;
    /*未选择项目文字色*/
    private int mDefaultTextColor;

    /*选择的位置*/
    private int mSelectedPosition = 0;

    public NumericPickerWheelView(Context context) {
        super(context);
        initView(context);
    }

    public NumericPickerWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NumericPickerWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    /**
     * 初始化设置
     *
     * @param context
     */
    private void initView(Context context) {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(infService);
        View view = inflater.inflate(R.layout.timepicker_list, this, true);
        mNumericListView = (ListView) view.findViewById(R.id.numeric_list_lv);
        mNumericListView.getLayoutParams().height = (int) (DEFAULT_ROW_COUNT *
                context.getResources().getDimension(R.dimen.time_picker_item_height));
        mNumericListView.setOnScrollListener(this);
        mSelectedTextColor = context.getResources().getColor(R.color.fontcor6);
        mDefaultTextColor = context.getResources().getColor(R.color.fontcor4);
    }

    /**
     * 初始化View字体大小，显示内容
     *
     * @param datas
     * @param selectedTextSize
     * @param textSize
     */
    public void setData(List<String> datas, int selectedTextSize, int textSize) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        mMinTextSize = textSize;
        mMaxTextSize = selectedTextSize;

        mNumericAdapter = new NumericPickerAdapter(getContext(), datas);
        mNumericListView.setAdapter(mNumericAdapter);
        mLastSelectedItem = datas.get(0);
        mNumericListView.setSelectionFromTop(mNumericAdapter.getListMiddleDisplayItem(0) - mDisplayRowCount / 2, 0);
    }

    /**
     * 重新设置显示内容
     *
     * @param datas
     * @param selected
     */
    public void reSetData(List<String> datas, int selected) {
        if (datas == null || datas.size() == 0) {
            mNumericAdapter.clearData();
            return;
        }
        int firstVisibleItem = mNumericListView.getFirstVisiblePosition();
        mLastSelectedItem = mNumericAdapter.getItem(firstVisibleItem + mDisplayRowCount / 2);
        mNumericAdapter.resetData(datas);
        mSelectedPosition = selected;
        mNumericListView.setSelectionFromTop(mNumericAdapter.getListMiddleDisplayItem(selected - mDisplayRowCount / 2), 0);
    }

    /**
     * 设置选中的项目位置
     *
     * @param selected
     */
    public void setSelectedPosition(int selected) {
        mLastSelectedItem = mNumericAdapter.getItem(mSelectedPosition);
        mSelectedPosition = selected;
        mNumericListView.setSelectionFromTop(mNumericAdapter.getListMiddleDisplayItem(selected) - mDisplayRowCount / 2, 0);
    }

    /**
     * 获得当前选中的项目值
     *
     * @return
     */
    public String getSelectedValue() {
        return mNumericAdapter.getItem(mSelectedPosition);
    }

    /**
     * 获得显示列表的第一项的值
     *
     * @return
     */
    public String getListStartValue() {
        return mNumericAdapter.getItem(0);
    }

    /**
     * 显示行数
     *
     * @param rowCount
     */
    public void setDisplayRowCount(int rowCount) {
        mDisplayRowCount = rowCount;
        mNumericListView.getLayoutParams().height = (int) (mDisplayRowCount *
                getContext().getResources().getDimension(R.dimen.time_picker_item_height));
    }

    /**
     * 设置选择项目变化监听
     *
     * @param listener
     */
    public void setOnSelectListener(onSelectListener listener) {
        mSelectListener = listener;
    }

    /**
     * 滚动了，选中的项目可能有变化
     */
    private void performSelect() {
        String newValue = mNumericAdapter.getItem(mSelectedPosition);
        if (mSelectListener != null) {
            mSelectListener.onSelect(this, newValue, mLastSelectedItem);
        }
        mLastSelectedItem = newValue;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 停止了滚动
            int firstVisibleItem = view.getFirstVisiblePosition();
            View firstVisibleView = view.getChildAt(0);
            if (firstVisibleView == null) {
                // 异常情况
                return;
            }
            int top = firstVisibleView.getTop();
            int listTop = view.getTop();
            int invisibleY = listTop - top;
            mLastSelectedItem = mNumericAdapter.getItem(mSelectedPosition);
            if (invisibleY == 0) {
                // 刚好停止在item头部
                mSelectedPosition = firstVisibleItem + mDisplayRowCount / 2;
                performSelect();
                return;
            }
            // 滚动到项目头部，防止停止时显示半个条目
            if (invisibleY > firstVisibleView.getHeight() / 2) {
                ((ListView) view).setSelectionFromTop(firstVisibleItem + 1, 0);
                mSelectedPosition = firstVisibleItem + 1 + mDisplayRowCount / 2;
            } else {
                ((ListView) view).setSelectionFromTop(firstVisibleItem, 0);
                mSelectedPosition = firstVisibleItem + mDisplayRowCount / 2;
            }
            performSelect();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int listViewHeight = view.getHeight();
        if (listViewHeight == 0) {
            return;
        }
        // List头部所在位置
        int listTop = view.getTop();
        int listViewMiddleY = listTop + listViewHeight / 2;
        for (int i = 0; i < visibleItemCount; i++) {
            View firstVisibleView = view.getChildAt(i);
            if (firstVisibleView == null) {
                continue;
            }
            // 根据滚动的偏移位置重新设置字号
            int viewMiddle = firstVisibleView.getTop() + firstVisibleView.getHeight() / 2;
            int distance = listViewMiddleY - viewMiddle;
            distance = distance > 0 ? distance : -distance;
            float scale = 1 - distance / (listViewHeight / 2.0f);
            int textSize = (int) ((mMaxTextSize - mMinTextSize) * scale) + mMinTextSize;
            // 通过设置TextPaint设置字体大小，防止再次调用setTextColor()时出现warning:"running second layout pass"
            TextPaint textPaint = ((TextView) firstVisibleView).getPaint();
            textPaint.setTextSize(textSize);
            if (i == visibleItemCount / 2) {
                // 中间的项目显示为选中的颜色
                ((TextView) firstVisibleView).setTextColor(mSelectedTextColor);
            } else {
                ((TextView) firstVisibleView).setTextColor(mDefaultTextColor);
            }
        }

    }

    /**
     * 展示内容的Adapter
     */
    private class NumericPickerAdapter extends BaseAdapter {

        private final static int MAX = 0x7FFFFFFF;
        private Context mContext;
        private ArrayList<String> mValueList = new ArrayList<String>();

        NumericPickerAdapter(Context context, List<String> valueList) {
            this.mContext = context;
            mValueList.clear();
            if (valueList != null) {
                mValueList.addAll(valueList);
            }
        }

        /**
         * 获取当前中间展示的项目内容
         *
         * @param listPos
         * @return
         */
        public int getListMiddleDisplayItem(int listPos) {
            if (mValueList.size() == 0) {
                return 0;
            }
            int middleItem = MAX / 2;
            middleItem = middleItem - middleItem % mValueList.size() + listPos;
            return middleItem;
        }

        /**
         * 重新设置显示内容
         *
         * @param valueList
         */
        public void resetData(List<String> valueList) {
            mValueList.clear();
            if (valueList != null) {
                mValueList.addAll(valueList);
            }
            notifyDataSetChanged();
        }

        /**
         * 清空所有展示内容
         */
        public void clearData() {
            mValueList.clear();
        }

        @Override
        public int getCount() {
            return MAX;
        }

        @Override
        public String getItem(int position) {
            if (mValueList.size() == 0) {
                return null;
            }
            int realPosition = position % mValueList.size();
            return mValueList.get(realPosition);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.timepicker_list_item, parent, false);
            }
            ((TextView) convertView).setText(getItem(position));
            return convertView;
        }

    }

    public interface onSelectListener {
        /**
         * 滚动停止，中间选中的项目变化了
         *
         * @param view
         * @param text
         * @param oldText
         */
        void onSelect(View view, String text, String oldText);
    }
}
