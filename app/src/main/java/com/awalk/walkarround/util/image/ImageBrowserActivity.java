package com.awalk.walkarround.util.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.photoview.HackyViewPager;
import com.awalk.walkarround.base.view.photoview.PhotoView;
import com.awalk.walkarround.util.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Richard on 2015/12/12.
 */
public class ImageBrowserActivity extends Activity implements View.OnClickListener {

    public static final String INTENT_DEFAULT_POSITION = "default_position";
    public static final String INTENT_CHOSE_PATHLIST = "path_list";
    public static final String INTENT_ORIGIN_PATHLIST = "origin_path_list";
    public static final String IMAGE_SEND_REQUEST = "image_send_request";
    public static final String INTENT_IMAGE_FROM_TYPE = "image_from_type";
    public static final String INTENT_IMAGE_FULLSIZED = "map_isFull";
    public static final String INTENT_IMAGE_IS_FULL_SIZE_OPTION = "image_is_full_size_option";
    public static final String INTENT_IMAGE_MAX_NUM = "image_max_num";
    public static final String INTENT_DISABLE_OK_BTN = "image_disable_ok_btn";

    public static final int _TYPE_FROM_MSG = 100;
    public static final int _TYPE_FROM_CAMERA = 102;
    public static final int _TYPE_FROM_PIC_SELECT = 103;

    private Logger logger = Logger.getLogger(ImageBrowserActivity.class.getSimpleName());
    private int mViewType = _TYPE_FROM_MSG;
    private boolean mIsFullSizeOption = false;
    private int mMaxNum = 0;
    private boolean mIsDisableOKBtn = false;

    private TextView mBackHintView;
    /*可展示的所有图片*/
    private ArrayList<String> mDisplayImageList = new ArrayList<String>();
    /*选中的图片*/
    private HashMap<String, Boolean> mChoseImageList = new HashMap<String, Boolean>();
    /*当前展示的图片在mDisplayImageList中的位置*/
    private int mCurrentImagePosition = 0;
    /*原图和选中是否勾选CheckBox*/
    private CheckBox mPicFullSizeCb, mPicSelectCb;

    private TextView mBtnSend;

    /*发送原图*/
    private HashSet<String> mOriginCheckedMap = new HashSet<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browser);
        // 数据
        if (!initIntentData(savedInstanceState, getIntent())) {
            finish();
            return;
        }
        // 初始化页面
        initView();
    }

    /**
     * Intent数据
     */
    private boolean initIntentData(Bundle savedInstanceState, Intent intent) {
        ArrayList<String> choseList = null;
        if (savedInstanceState != null) {
        } else if (intent != null) {
            mDisplayImageList = intent.getStringArrayListExtra(INTENT_ORIGIN_PATHLIST);
            mCurrentImagePosition = intent.getIntExtra(INTENT_DEFAULT_POSITION, 0);
            choseList = intent.getStringArrayListExtra(INTENT_CHOSE_PATHLIST);
            mViewType = intent.getIntExtra(INTENT_IMAGE_FROM_TYPE, _TYPE_FROM_MSG);
            mIsFullSizeOption = intent.getBooleanExtra(INTENT_IMAGE_IS_FULL_SIZE_OPTION, false);
            mMaxNum = intent.getIntExtra(INTENT_IMAGE_MAX_NUM, 0);
            mIsDisableOKBtn = intent.getBooleanExtra(INTENT_DISABLE_OK_BTN, false);
        }
        if (choseList != null) {
            for (String choseItem : choseList) {
                mChoseImageList.put(choseItem, true);
            }
        }
        return (mDisplayImageList != null && mDisplayImageList.size() > 0);
    }

    private void initView() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).findViewById(R.id.more_iv).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.img_pic_browser);

        mBackHintView = (TextView)title.findViewById(R.id.back_rl).findViewById(R.id.left_tx);
        mBtnSend = (TextView) title.findViewById(R.id.more_rl).findViewById(R.id.right_tx);
        mBtnSend.setOnClickListener(this);

        if(mIsDisableOKBtn) {
            mBtnSend.setVisibility(View.GONE);
        } else {
            mBtnSend.setVisibility(View.VISIBLE);
        }
        mPicFullSizeCb = (CheckBox) findViewById(R.id.pic_full_size_btn);
        if (!mIsFullSizeOption) {
            mPicFullSizeCb.setVisibility(View.GONE);
        } else {
            mPicFullSizeCb.setVisibility(View.VISIBLE);
        }

        //Set the select check box as gone now.
        mPicSelectCb = (CheckBox) findViewById(R.id.pic_select_check);
        mPicSelectCb.setVisibility(View.GONE);
        mBackHintView.setOnClickListener(this);
        //mBtnSend.setOnClickListener(this);

        //int sendingCount = mDisplayImageList.size();
        //mBackHintView.setText((mCurrentImagePosition + 1) + "/" + sendingCount);
        if (!mIsFullSizeOption) {
            mBtnSend.setText(getResources().getString(R.string.img_select_pics_finish_multi));
        } else {
            mBtnSend.setText(getResources().getString(R.string.img_select_pics_send_multi));
        }
        SamplePagerAdapter samplePagerAdapter = new SamplePagerAdapter(this);
        HackyViewPager mViewPager = (HackyViewPager) findViewById(R.id.xx_browser);
        mViewPager.setAdapter(samplePagerAdapter);
        mViewPager.setCurrentItem(mCurrentImagePosition);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int page) {
                mCurrentImagePosition = page;
                switch (mViewType) {
                    case _TYPE_FROM_CAMERA:
                    case _TYPE_FROM_PIC_SELECT:
                        String filePath = mDisplayImageList.get(mCurrentImagePosition);
                        if (mOriginCheckedMap.contains(filePath)) {
                            mPicFullSizeCb.setChecked(true);
                            mPicFullSizeCb.setText(getString(R.string.img_pic_full_size) + getFileLength(filePath));
                        } else {
                            mPicFullSizeCb.setChecked(false);
                            mPicFullSizeCb.setText(R.string.img_pic_full_size);
                        }
                        mPicSelectCb.setChecked(mChoseImageList.containsKey(filePath));
                        break;
                    default:
                        break;
                }
                //mBackHintView.setText((mCurrentImagePosition + 1) + "/" + mDisplayImageList.size());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        mPicFullSizeCb.setOnClickListener(this);
        mPicSelectCb.setOnClickListener(this);

        switch (mViewType) {
            case _TYPE_FROM_MSG:
                findViewById(R.id.title).setVisibility(View.GONE);
                findViewById(R.id.gallery_tool_bar_preview).setVisibility(View.GONE);
                break;
            case _TYPE_FROM_CAMERA:
            case _TYPE_FROM_PIC_SELECT:
                if(mIsDisableOKBtn) {
                    mBtnSend.setVisibility(View.GONE);
                } else {
                    mBtnSend.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    private String getFileLength(String filePath) {
        DecimalFormat df = new DecimalFormat("0.00");
        File file = new File(filePath);
        if (!file.exists())
            return "";
        long fileLength = file.length();
        String result;
        if (fileLength > 1024 * 1024) {
            result = "(" + df.format((float) fileLength / 1024 / 1024) + ")MB";
        } else {
            result = "(" + df.format((float) fileLength / 1024) + ")KB";
        }
        return result;
    }

    private class SamplePagerAdapter extends PagerAdapter {

        private Context mContext;

        SamplePagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            if (mViewType == _TYPE_FROM_MSG) {
                return mDisplayImageList.size();
            } else if (mViewType == _TYPE_FROM_CAMERA) {
                return 1;
            } else if (mViewType == _TYPE_FROM_PIC_SELECT) {
                return mDisplayImageList.size();
            } else {
                return 0;
            }
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            View view = mInflater.inflate(R.layout.activity_image_browser_item, null);

            PhotoView photoView = (PhotoView) view.findViewById(R.id.iv_browser);

            String imagePath = mDisplayImageList.get(position);
            ImageLoaderManager.displayImage(imagePath, R.drawable.default_image, photoView);
            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl:
                onBackPressed();
                break;
            case R.id.right_tx:
                switch (mViewType) {
                    case _TYPE_FROM_MSG:
                        break;
                    case _TYPE_FROM_CAMERA:
                    case _TYPE_FROM_PIC_SELECT:
                        if (mChoseImageList.size() > 0) {
                            mBtnSend.setVisibility(View.VISIBLE);
                            Intent intent = new Intent();
                            ArrayList<String> choseList = new ArrayList<String>();
                            String[] choseArray = new String[mChoseImageList.size()];
                            mChoseImageList.keySet().toArray(choseArray);
                            choseList.addAll(Arrays.asList(choseArray));
                            intent.putStringArrayListExtra(INTENT_CHOSE_PATHLIST, choseList);
                            intent.putExtra(IMAGE_SEND_REQUEST, true);
                            intent.putExtra(INTENT_IMAGE_FULLSIZED, mOriginCheckedMap);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(ImageBrowserActivity.this, R.string.img_hint_please_select_pic, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case R.id.pic_full_size_btn:
                switch (mViewType) {
                    case _TYPE_FROM_MSG:
                        break;
                    case _TYPE_FROM_CAMERA:
                    case _TYPE_FROM_PIC_SELECT:
                        String imagePath = mDisplayImageList.get(mCurrentImagePosition);
                        if (mPicFullSizeCb.isChecked()) {
                            mPicFullSizeCb.setText(getString(R.string.img_pic_full_size) + getFileLength(imagePath));
                            mOriginCheckedMap.add(imagePath);
                        } else {
                            mPicFullSizeCb.setText(R.string.img_pic_full_size);
                            mOriginCheckedMap.remove(imagePath);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case R.id.pic_select_check:
                if (!mPicSelectCb.isChecked()) {
                    // 取消选择
                    mChoseImageList.remove(mDisplayImageList.get(mCurrentImagePosition));
                    if (!mIsFullSizeOption) {
                        mBtnSend.setText(getResources().getString(R.string.img_select_pics_finish_multi, mChoseImageList.size(), mMaxNum));
                    } else {
                        mBtnSend.setText(getResources().getString(R.string.img_select_pics_send_multi));
                    }
                } else {
                    mChoseImageList.put(mDisplayImageList.get(mCurrentImagePosition), true);
                    if (!mIsFullSizeOption) {
                        mBtnSend.setText(getResources().getString(R.string.img_select_pics_finish_multi, mChoseImageList.size(), mMaxNum));
                    } else {
                        mBtnSend.setText(getResources().getString(R.string.img_select_pics_send_multi));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        switch (mViewType) {
            case _TYPE_FROM_MSG:
                finish();
                break;
            case _TYPE_FROM_CAMERA:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case _TYPE_FROM_PIC_SELECT:
                Intent intent = new Intent();
                ArrayList<String> choseList = new ArrayList<String>();
                String[] choseArray = new String[mChoseImageList.size()];
                mChoseImageList.keySet().toArray(choseArray);
                choseList.addAll(Arrays.asList(choseArray));
                intent.putStringArrayListExtra(INTENT_CHOSE_PATHLIST, choseList);
                intent.putExtra(INTENT_IMAGE_FULLSIZED, mOriginCheckedMap);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }
}
