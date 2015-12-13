package com.example.walkarround.util.image;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkarround.R;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;

/**
 * Created by Richard on 2015/12/12.
 */
public class ImageChooseActivity extends Activity implements View.OnClickListener, ImageListAdapter.ImageItemListener, AdapterView.OnItemClickListener {

    private static final Logger logger = Logger.getLogger(ImageChooseActivity.class.getSimpleName());

    public static final String IMAGE_COUNT = "image_count";

    public static final String IMAGE_CHOOSE_TYPE = "from";
    // 可多选
    public static final int FROM_MESSAGE_CODE = 0;
    // 单选
    public static final int FROM_MORE_CONFIG = 1;

    // 是否是无选择原图选项
    public static final String IS_FULL_SIZE_OPTION = "is full size option";
    // 已选择的图片
    public static final String HAVE_CHOSEN_NUM = "have chosen num";
    // 照相机拍照图片地址
    public static final String CAMERA_IMAGE_PATH = "camera image path";

    /* 打开相机 */
    public static final int REQUEST_CODE_OPEN_CAMERA = 1;
    /* 浏览 */
    public static final int REQUEST_CODE_PREVIEW_IMAGE = 2;
    /* 浏览相机刚拍的图片 */
    public static final int REQUEST_CODE_PREVIEW_CAMERA = 3;

    /* 最多9张图片*/
    public static final int MAX_SELECTED_COUNT = 9;

    /* 是否需要预览 */
    private boolean isNeedPreView = false;
    /* 页面展示类型 */
    private int mViewType;

    /* 预览按钮 */
    private Button mPreviewBtn;
    /* 选择确定按钮 */
    private Button mChoseOkBtn;

    /* 选择文件夹 */
    private PopupWindow mDirectoryPopup;

    /* 利用相机刚拍图片 */
    private String mCameraImagePath = null;

    private ImageListAdapter mImageListAdapter;
    private HashMap<String, ArrayList<String>> directorys;
    private HashSet<String> mFullSizedMap = new HashSet<String>();

    private int mMaxNum;
    private boolean mIsFullSizeOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choose);
        // 数据
        initIntentData(savedInstanceState, getIntent());
        // 初始化画面固定部分
        initBaseView();
        // 获取图片数据
        GetImagePathTask thread = new GetImagePathTask(this, mViewType == FROM_MESSAGE_CODE);
        thread.execute(isNeedPreView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(IMAGE_CHOOSE_TYPE, mViewType);
        outState.putBoolean(IS_FULL_SIZE_OPTION, mIsFullSizeOption);
        outState.putString(CAMERA_IMAGE_PATH, mCameraImagePath);
    }

    /**
     * Intent数据
     */
    private void initIntentData(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState != null) {
            mViewType = savedInstanceState.getInt(IMAGE_CHOOSE_TYPE, FROM_MESSAGE_CODE);
            int num = savedInstanceState.getInt(HAVE_CHOSEN_NUM, 0);
            mMaxNum = MAX_SELECTED_COUNT - num;
            mIsFullSizeOption = savedInstanceState.getBoolean(IS_FULL_SIZE_OPTION, false);
            mCameraImagePath = savedInstanceState.getString(CAMERA_IMAGE_PATH, "");
        } else if (intent != null) {
            mViewType = intent.getIntExtra(IMAGE_CHOOSE_TYPE, FROM_MESSAGE_CODE);
            int imagecount = intent.getIntExtra(IMAGE_COUNT,MAX_SELECTED_COUNT);
            int num = intent.getIntExtra(HAVE_CHOSEN_NUM, 0);
            mMaxNum = imagecount - num;
            mIsFullSizeOption = intent.getBooleanExtra(IS_FULL_SIZE_OPTION, false);
        }
        if (mViewType == FROM_MESSAGE_CODE) {
            isNeedPreView = true;
        }
    }

    /**
     * 初始化画面
     */
    private void initBaseView() {
        // 头部返回按钮
        findViewById(R.id.attachment_photo_back).setOnClickListener(this);
        // 选择确定按钮
        mChoseOkBtn = (Button) findViewById(R.id.choose_ok_btn);
        // 预览按钮
        mPreviewBtn = (Button) findViewById(R.id.preview_btn);
        switch (mViewType) {
            case FROM_MESSAGE_CODE:
                mChoseOkBtn.setVisibility(View.VISIBLE);
                mPreviewBtn.setText(R.string.img_select_at_most_pic);
                mChoseOkBtn.setOnClickListener(this);
                mPreviewBtn.setOnClickListener(this);
                break;
            case FROM_MORE_CONFIG:
                findViewById(R.id.bottomLayout).setVisibility(View.GONE);
                mChoseOkBtn.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        // 根据文件夹过滤图片
        LinearLayout mFilterLayout = (LinearLayout) findViewById(R.id.directory_filter);
        if (isNeedPreView) {
            mFilterLayout.setOnClickListener(this);
            mFilterLayout.setEnabled(false);
        } else {
            mFilterLayout.setVisibility(View.GONE);
        }

        boolean bShowCheck = mViewType != FROM_MORE_CONFIG;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        mImageListAdapter = new ImageListAdapter(ImageChooseActivity.this, bShowCheck, screenW / 3, this, mMaxNum);
        GridView gridView = (GridView) findViewById(R.id.image_list_gridview);
        gridView.setAdapter(mImageListAdapter);
    }

    // 将选取好的图片路径传送回BuildMessageActivity
    protected void sendImages(ArrayList<String> sendingList) {
        if (sendingList == null || sendingList.size() == 0) {
            Toast.makeText(ImageChooseActivity.this, R.string.img_select_at_least_1_pic, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(ImageBrowserActivity.INTENT_CHOSE_PATHLIST, sendingList);
            intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_FULLSIZED, mFullSizedMap);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 从相册中选择图片
        if (requestCode == REQUEST_CODE_PREVIEW_IMAGE) {
            if (resultCode != RESULT_OK) {
                return;
            }
            ArrayList<String> sendingList = data.getStringArrayListExtra(ImageBrowserActivity.INTENT_CHOSE_PATHLIST);
            mImageListAdapter.refreshCheckedList(sendingList);
            mFullSizedMap = (HashSet<String>) data.getSerializableExtra(ImageBrowserActivity.INTENT_IMAGE_FULLSIZED);
            if (data.getBooleanExtra(ImageBrowserActivity.IMAGE_SEND_REQUEST, false)) {
                sendImages(sendingList);
            }
        } else if (requestCode == REQUEST_CODE_OPEN_CAMERA) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (TextUtils.isEmpty(mCameraImagePath)) {
                logger.e("camera get image null...");
                return;
            }

            // 打开照相机进行拍照
            Intent intent = new Intent(ImageChooseActivity.this, ImageBrowserActivity.class);
            ArrayList<String> originFilePath = new ArrayList<String>();
            originFilePath.add(mCameraImagePath);
            intent.putExtra(ImageBrowserActivity.INTENT_ORIGIN_PATHLIST, originFilePath);
            intent.putExtra(ImageBrowserActivity.INTENT_CHOSE_PATHLIST, originFilePath);
            intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_FROM_TYPE, ImageBrowserActivity._TYPE_FROM_CAMERA);
            intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_IS_FULL_SIZE_OPTION, mIsFullSizeOption);
            intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_MAX_NUM, 1);
            startActivityForResult(intent, REQUEST_CODE_PREVIEW_CAMERA);
        } else if (requestCode == REQUEST_CODE_PREVIEW_CAMERA) {
            if (resultCode != RESULT_OK) {
                return;
            }
            // 拍照后返回的图片
            if (data.getBooleanExtra(ImageBrowserActivity.IMAGE_SEND_REQUEST, false)) {
                mFullSizedMap = (HashSet<String>) data
                        .getSerializableExtra(ImageBrowserActivity.INTENT_IMAGE_FULLSIZED);
                ArrayList<String> pathList = data.getExtras()
                        .getStringArrayList(ImageBrowserActivity.INTENT_CHOSE_PATHLIST);
                ArrayList<String> sendingList = new ArrayList<String>();
                sendingList.addAll(pathList);
                sendImages(sendingList);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 初始化筛选
     */
    private void initPopupViewDir(View popupContentView) {
        DirectoryFilterAdapter filterAdapter = new DirectoryFilterAdapter(ImageChooseActivity.this, initAdapterList(),
                new String[] { "firstpath", "directoryname", "count" }, new int[] { R.id.album_image,
                R.id.album_title_textView, R.id.album_child_count_textView });
        ListView filterListView = (ListView) popupContentView.findViewById(R.id.image_chooser_listView);
        filterListView.setOnItemClickListener(ImageChooseActivity.this);
        filterListView.setAdapter(filterAdapter);
    }

    private List<Map<String, String>> initAdapterList() {
        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        for (Map.Entry<String, ArrayList<String>> entry : directorys.entrySet()) {
            if (entry.getValue().size() > 0) {
                Map<String, String> tempMap = new HashMap<String, String>();
                int start = entry.getKey().contains("/") ? entry.getKey().lastIndexOf('/') + 1 : 0;
                tempMap.put("directoryname", entry.getKey().substring(start));
                tempMap.put("firstpath", entry.getValue().get(0));
                tempMap.put(
                        "count",
                        getResources().getString(R.string.img_pic_total_count,
                                String.valueOf(entry.getValue().size())));
                maps.add(tempMap);
            }
        }
        return maps;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.attachment_photo_back:
                // 返回
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.choose_ok_btn:
                // 发送
                sendImages(mImageListAdapter.getCheckedList());
                break;
            case R.id.directory_filter:
                // 更改过滤文件夹
                if (mDirectoryPopup == null) {
                    View menu = getLayoutInflater().inflate(R.layout.image_chooser_menu_popup, null, true);
                    initPopupViewDir(menu);
                    mDirectoryPopup = new PopupWindow(menu, LinearLayout.LayoutParams.MATCH_PARENT, 1000, true);
                    mDirectoryPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.public_bg_light_nm));
                }
                if (!mDirectoryPopup.isShowing()) {
                    mDirectoryPopup.showAsDropDown(view);
                }
                break;
            case R.id.preview_btn:
                // 预览
                Intent intent = new Intent();
                intent.setClass(ImageChooseActivity.this, ImageBrowserActivity.class);
                ArrayList<String> selectedList = mImageListAdapter.getCheckedList();
                intent.putExtra(ImageBrowserActivity.INTENT_CHOSE_PATHLIST, selectedList);
                intent.putExtra(ImageBrowserActivity.INTENT_ORIGIN_PATHLIST, selectedList);
                intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_FULLSIZED, mFullSizedMap);
                intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_FROM_TYPE, ImageBrowserActivity._TYPE_FROM_PIC_SELECT);
                intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_IS_FULL_SIZE_OPTION, mIsFullSizeOption);
                intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_MAX_NUM, mMaxNum);
                startActivityForResult(intent, REQUEST_CODE_PREVIEW_IMAGE);
                break;
            default:
                break;
        }

    }

    @Override
    public void onChoseCountChange(int choseCount) {
        // 选中文件个数变化
        if (choseCount == 0) {
            mPreviewBtn.setEnabled(false);
            mPreviewBtn.setText(R.string.img_select_pics_preview);
            mChoseOkBtn.setText(R.string.com_select);
        } else {
            mPreviewBtn.setEnabled(true);
            mPreviewBtn.setText(String.format(getResources().getString(R.string.img_select_pics_preview_multi)));
            if (!mIsFullSizeOption) {
                mChoseOkBtn.setText(String
                        .format(getResources().getString(R.string.img_select_pics_finish_multi)));
            } else {
                mChoseOkBtn.setText(String
                        .format(getResources().getString(R.string.img_select_pics_send_multi)));
            }
        }
    }

    @Override
    public void onCameraClick() {
        // 打开照相机进行拍照
        mCameraImagePath = CommonUtils.createCameraTakePicFile();
        if (TextUtils.isEmpty(mCameraImagePath)) {
            Toast.makeText(this, R.string.img_open_camera_fail, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = Uri.fromFile(new File(mCameraImagePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_CODE_OPEN_CAMERA);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        // 更改了过滤文件夹
        List<String> list = directorys.get(directorys.keySet().toArray()[position]);
        mImageListAdapter.setImageListInfo(list);
        mImageListAdapter.notifyDataSetChanged();
        String tempTile = directorys.keySet().toArray()[position].toString();
        TextView mDirectoryNameTextView = (TextView) findViewById(R.id.filter_title);
        mDirectoryNameTextView
                .setText(tempTile.substring((tempTile.contains("/") ? tempTile.lastIndexOf('/') + 1 : 0)));
        if (mDirectoryPopup != null && mDirectoryPopup.isShowing()) {
            mDirectoryPopup.dismiss();
        }
    }

    public enum PhotoResource {
        ALL, ONLY_CAMERA
    }

    public class GetImagePathTask extends AsyncTask<Boolean, ArrayList<String>, ArrayList<String>> {

        private HashMap<String, Boolean> mSupportImage = new HashMap<String, Boolean>();

        GetImagePathTask(Context context, boolean needFilterImage) {
            if (needFilterImage) {
                String[] suppotImage = context.getResources().getStringArray(R.array.msg_support_image);
                for (String imageExt : suppotImage) {
                    mSupportImage.put(imageExt, true);
                }
            }
        }

        @Override
        protected ArrayList<String> doInBackground(Boolean... booleans) {
            PhotoResource resource = booleans[0] ? PhotoResource.ALL : PhotoResource.ONLY_CAMERA;
            return getImagePathList(resource);
        }

        @Override
        protected void onPostExecute(ArrayList<String> resultList) {
            mImageListAdapter.setImageListInfo(resultList);
            mImageListAdapter.notifyDataSetChanged();
            if (isNeedPreView) {
                findViewById(R.id.directory_filter).setEnabled(true);
            }
        }

        /**
         * 获取相册路径列表
         *
         * @return
         */
        public ArrayList<String> getImagePathList(PhotoResource resource) {
            ArrayList<String> list = new ArrayList<String>();
            Cursor cursor = null;
            String[] Colums;
            switch (resource) {
                case ALL:
                    Colums = new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED };
                    cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Colums, null, null,
                            MediaStore.Images.Media.DATE_ADDED + " DESC");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String tempString = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            if (isSupportImageType(mSupportImage, tempString)) {
                                list.add(tempString);
                            }
                        }
                        cursor.close();
                    }
                    directorys = getDirectorys(list);
                    break;
                case ONLY_CAMERA:
                    // photoDirManager = new PhotoDirectoryManager();
                    // list = photoDirManager.getFiles();
                    Colums = new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED };
                    cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Colums,
                            MediaStore.Images.Media.DATA + " like '%DCIM%'" + " OR " + MediaStore.Images.Media.DATA + " like '%Camera%' ", null,
                            MediaStore.Images.Media.DATE_ADDED);
                    if (cursor != null) {
                        cursor.moveToLast();
                        do {
                            String tempString = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            // 过滤掉不支持的图片格式
                            if (isSupportImageType(mSupportImage, tempString)) {
                                list.add(tempString);
                            }
                        } while (cursor.moveToPrevious());
                    }
                    if (cursor != null)
                        cursor.close();
                    break;
                default:
                    break;
            }
            return list;
        }

        private HashMap<String, ArrayList<String>> getDirectorys(ArrayList<String> allImagePahts) {
            if (allImagePahts == null)
                return null;
            HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
            result.put("所有图片", new ArrayList<String>());
            for (String imagePath : allImagePahts) {
                result.get("所有图片").add(imagePath);
                File file = new File(imagePath);
                String parentPath = file.getParent();
                if (!result.containsKey(parentPath)) {
                    result.put(parentPath, new ArrayList<String>());
                }
                result.get(parentPath).add(imagePath);
            }
            return result;
        }

        private boolean isSupportImageType(HashMap<String, Boolean> supportImage, String imagePath) {
            if (supportImage.size() == 0) {
                return true;
            }
            int dot = imagePath.lastIndexOf('.');
            if ((dot > -1) && (dot < (imagePath.length() - 1))
                    && supportImage.containsKey(imagePath.substring(dot + 1))) {
                return true;
            }
            return false;
        }
    }

}
