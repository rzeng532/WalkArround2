package com.example.walkarround.message.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.example.walkarround.R;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.image.ImageLoaderManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author cmcc
 * 
 */
public class ImageListAdapter extends BaseAdapter implements OnClickListener {
    private Logger logger = Logger.getLogger(ImageListAdapter.class.getSimpleName());

    private Context mContext;
    /*图片path信息*/
    private List<String> mUrlsArrayList = new ArrayList<String>();
    private LayoutInflater mInflater;
    private HashMap<String, Integer> mChoseImageList = new HashMap<String, Integer>();
    private int mImageViewWidth;
    private boolean bShowCheckBox;
    private ImageItemListener mImageListener;
    private int mMaxNum;
    private DisplayImageOptions imageOptions;
    private DisplayImageOptions defaultOptions;

    public class ViewHolder {
        public ImageView imgView;
        public ImageView checkBox;
    }

    /**
     * 更新选中的项目
     *
     * @param choseList
     */
    public void refreshCheckedList(List<String> choseList) {
        mChoseImageList.clear();
        if (choseList == null || choseList.size() == 0) {
            if (mImageListener != null) {
                mImageListener.onChoseCountChange(0);
            }
            return;
        }
        int totalCount = getCount();
        for (String choseItem : choseList) {
            for (int i = 1; i < totalCount; i++) {
                if (choseItem.equals(getItem(i))) {
                    mChoseImageList.put(choseItem, i);
                    break;
                }
            }
        }
        if (mImageListener != null) {
            mImageListener.onChoseCountChange(mChoseImageList.size());
        }
        notifyDataSetChanged();
    }

    /**
     * 获取选中的项目
     *
     * @return
     */
    public ArrayList<String> getCheckedList() {
        int count = mChoseImageList.size();
        Integer[] valueSet = new Integer[count];
        mChoseImageList.values().toArray(valueSet);
        valueSet = insertSort(valueSet);
        ArrayList<String> choseList = new ArrayList<String>();
        if (valueSet != null && valueSet.length > 0) {
            for (Integer position : valueSet) {
                choseList.add(getItem(position));
            }
        }
        return choseList;
    }

    /**
     * 插入排序
     */
    private Integer[] insertSort(Integer[] sortList) {
        int listLength = sortList == null ? 0 : sortList.length;
        int temp = 0;
        for (int i = 1; i < listLength; i++) {
            int j = i - 1;
            temp = sortList[i];
            for (; j >= 0 && temp < sortList[j]; j--) {
                //将大于temp的值整体后移一个单位
                sortList[j + 1] = sortList[j];
            }
            sortList[j + 1] = temp;
        }
        return sortList;
    }

    public ImageListAdapter(Context con, boolean bShowCheckBox, int imageViewWidth,
                            ImageItemListener imageListener, int maxNum) {
        this.mContext = con;
        this.bShowCheckBox = bShowCheckBox;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageViewWidth = imageViewWidth;
        mImageListener = imageListener;
        mMaxNum = maxNum;
        imageOptions = ImageLoaderManager.getDisplayImageOptions(R.drawable.mail_picture_attachfile_icon);

        defaultOptions = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .showImageOnLoading(R.drawable.message_icon_pic_cam)
                .showImageOnFail(R.drawable.message_icon_pic_cam)
                .showImageForEmptyUri(R.drawable.message_icon_pic_cam)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public void setImageListInfo(List<String> paths) {
        mUrlsArrayList.clear();
        if (paths != null && paths.size() > 0) {
            mUrlsArrayList.addAll(paths);
        }
        refreshCheckedList(null);
    }

    @Override
    public int getCount() {
        return mUrlsArrayList.size() + 1;
    }

    @Override
    public String getItem(int position) {
        return mUrlsArrayList.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.image_list_item_view, null);
            holder.imgView = (ImageView) convertView.findViewById(R.id.image_item_iv);
            holder.imgView.setLayoutParams(new RelativeLayout.LayoutParams(mImageViewWidth, mImageViewWidth));
            holder.checkBox = (ImageView) convertView.findViewById(R.id.chosen_state_iv);
            if (bShowCheckBox) {
                holder.checkBox.setOnClickListener(this);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
            holder.imgView.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imgView.setTag(R.id.image_item_iv, position);
        holder.checkBox.setTag(R.id.chosen_state_iv, position);
        if (position != 0) {
            String imagePath = getItem(position);
            logger.d(imagePath);
            if (null != imagePath && !imagePath.toLowerCase().startsWith("http")) {
                String url = "file://"+imagePath;
                ImageLoader.getInstance().displayImage(url, holder.imgView, imageOptions);
            }
            holder.imgView.setPadding(0, 0, 0, 0);
            if (bShowCheckBox) {
                holder.checkBox.setVisibility(View.VISIBLE);
                if (mChoseImageList.containsKey(imagePath)) {
                    holder.imgView.setColorFilter(R.color.bgcor2);
                    holder.checkBox.setSelected(true);
                } else {
                    holder.imgView.clearColorFilter();
                    holder.checkBox.setSelected(false);
                }
            }
        } else {
            //gridview中第一个item设为点击拍照的入口
            String imagePath = "drawable://" + R.drawable.message_icon_pic_cam;
            ImageLoader.getInstance().displayImage(imagePath, holder.imgView, defaultOptions);
            int paddingTop = CommonUtils.dip2px(convertView.getContext(), 40);
            holder.imgView.setPadding(paddingTop, paddingTop, paddingTop,
                    CommonUtils.dip2px(convertView.getContext(), 50));
            holder.checkBox.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_item_iv:
            case R.id.chosen_state_iv:
                int position = (Integer) view.getTag(view.getId());
                if (position == 0) {
                    // 相机
                    if (mImageListener != null) {
                        mImageListener.onCameraClick();
                    }
                } else {
                    String imagePath = getItem(position);
                    if (mChoseImageList.containsKey(imagePath)) {
                        mChoseImageList.remove(imagePath);
                    } else {
                        if (mChoseImageList.size() >= mMaxNum) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.msg_select_at_most_pic, mMaxNum),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        mChoseImageList.put(imagePath, position);
                    }
                    notifyDataSetChanged();
                    if (mImageListener != null) {
                        mImageListener.onChoseCountChange(mChoseImageList.size());
                    }
                }
                break;
            default:
                break;
        }
    }

    public interface ImageItemListener {
        /**
         * 选中个数变化了
         */
        public void onChoseCountChange(int choseCount);

        /**
         * 点击了相机
         *
         */
        public void onCameraClick();
    }

}
