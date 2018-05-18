/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.base.view;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.awalk.walkarround.R;
import com.awalk.walkarround.util.image.ImageLoaderManager;

/**
 * @author Richard
 */
public class PortraitView extends LinearLayout implements View.OnClickListener{

    private ImageView mImageView;
    private TextView mTextView;
    private ImageView mCheckBox;
    private int mCheckedResId = R.drawable.public_icon_list_checkbox_on;
    private int mUncheckedResId = R.drawable.public_icon_list_checkbox_off;
    private View.OnClickListener mCheckBoxOnClickListener;
    private View.OnClickListener mPhotoOnClickListener;

    public PortraitView(Context context) {
        super(context);
        initView();
    }

    public PortraitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PortraitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infService);
        View view = inflater.inflate(R.layout.photo_view, this, true);
        mImageView = (ImageView) view.findViewById(R.id.photo_iv);
        mTextView = (TextView) view.findViewById(R.id.name_logogram_tv);
        mCheckBox = (ImageView) view.findViewById(R.id.select_iv);
        mCheckBox.setOnClickListener(this);
    }

    public void setBaseData(String name, String cachPhotoKey, String namePinyin, int defaultResId) {
        setBaseData(name, cachPhotoKey, namePinyin, defaultResId, false);
    }

    public void setBaseData(String name, String cachPhotoKey, String namePinyin, int defaultResId, final boolean toGray) {
        mImageView.setTag(null);
        int resId = defaultResId > 0 ? defaultResId : R.drawable.default_profile_portrait;

        if (!TextUtils.isEmpty(cachPhotoKey)) {
            // 头像
            mTextView.setVisibility(GONE);
            mImageView.setVisibility(VISIBLE);
            mImageView.setTag(cachPhotoKey);
            if (toGray) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                mImageView.setColorFilter(filter);
            } else {
                mImageView.clearColorFilter();
            }
            ImageLoaderManager.displayImage(cachPhotoKey, resId, mImageView);
        }
//        else if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(namePinyin)) {
//            // 显示名字最后一个字
//            mTextView.setVisibility(VISIBLE);
//            mImageView.setVisibility(GONE);
//            GradientDrawable myGrad = (GradientDrawable) mTextView.getBackground();
//            if (toGray) {
//                myGrad.setColor(getResources().getColor(R.color.fontcor5));
//            } else {
//                myGrad.setColor(CommonUtils.switchNameToColor(getContext(), namePinyin));
//            }
//            mTextView.setText(getNameLogo(name, cachPhotoKey));
//        }
        else {
            mTextView.setVisibility(GONE);
            mImageView.setVisibility(VISIBLE);
            if (toGray) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                mImageView.setColorFilter(filter);
            } else {
                mImageView.clearColorFilter();
            }
            ImageLoaderManager.displayImage(null, resId, mImageView);
        }
    }

    /**
     * 获取没有图片时显示字符串
     *
     * @param name
     * @param phoneNum
     * @return
     */
    private String getNameLogo(String name, String phoneNum) {
        String nameLogo = "";
        if (name != null && name.length() > 0) {
            nameLogo = name.substring(name.length() - 1);
            // } else if (phoneNum != null && phoneNum.length() > 0) {
            // int start = phoneNum.length() > LOGO_DISPLAY_NUM_LENGTH ?
            // phoneNum.length() - LOGO_DISPLAY_NUM_LENGTH : 0;
            // nameLogo = phoneNum.substring(start);
        }
        return nameLogo;
    }

    /**
     * @描述：设置CheckBox的背景图片
     */
    public void setCheckBoxResId(int checkedResId, int uncheckResId) {
        mCheckedResId = checkedResId;
        mUncheckedResId = uncheckResId;
        if (isChecked()) {
            mCheckBox.setImageResource(mCheckedResId);
        } else {
            mCheckBox.setImageResource(mUncheckedResId);
        }
    }

    /**
     * @描述：设置是否选中
     */
    public void setChecked(boolean isChecked) {
        mCheckBox.setSelected(isChecked);
        if (isChecked) {
            mCheckBox.setImageResource(mCheckedResId);
        } else {
            mCheckBox.setImageResource(mUncheckedResId);
        }
    }

    /**
     * @描述：是否选中
     */
    public boolean isChecked() {
        return mCheckBox.isSelected();
    }

    /**
     * @描述：设置选中按钮是否可见
     */
    public void setCheckBoxVisibility(int visibility) {
        if (mCheckBox.getVisibility() == visibility) {
            return;
        }
        mCheckBox.setVisibility(visibility);
    }

    /**
     * @描述：点击CheckBox选中事件
     */
    public void setCheckBoxOnClickListener(View.OnClickListener onClick) {
        mCheckBoxOnClickListener = onClick;
    }

    /**
     * @描述：点击头像事件
     */
    public void setPhotoOnClickListener(View.OnClickListener onClick) {
        mPhotoOnClickListener = onClick;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_iv:
                setChecked(!isChecked());
                if (mCheckBoxOnClickListener != null) {
                    mCheckBoxOnClickListener.onClick(this);
                }
                break;
            default:
                if (mPhotoOnClickListener != null) {
                    mPhotoOnClickListener.onClick(this);
                }
                break;
        }
    }

    /**
     * @描述：设置是否可响应点击
     */
    public void setCheckBoxClickable(boolean clickable) {
        mCheckBox.setClickable(clickable);
    }
}
