package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.example.walkarround.location.activity.LocationActivity;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.location.model.GeoData;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.image.ImageBrowserActivity;
import com.example.walkarround.util.image.ImageChooseActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Richard on 2015/12/9.
 */
public class DetailInformationActivity extends Activity implements View.OnClickListener {

    private TextView mTvTitle;
    private PortraitView mMyPortrait;
    private TextView mTvUserName;
    private TextView mTvMobile;
    private TextView mTvBirth;
    private TextView mTvGendle;
    private TextView mTvSignature;
    private TextView mTvLocation;

    private View mVPortrait;
    private View mVUserName;
    private View mVMobileNum;
    private View mVBirthday;
    private View mVGendle;
    private View mVSignature;
    private View mVLocation;

    private final int REQUEST_CODE_PICTURE_CHOOSE = 100;
    private final int REQUEST_CODE_PICTURE_CUT = 101;
    private final int REQUEST_CODE_LOCATION = 102;
    private final int REQUEST_CODE_EDIT_STR = 103;

    private Logger logger = Logger.getLogger(DetailInformationActivity.class.getSimpleName());
    private MyProfileInfo myProfileInfo = null;

    private Uri headUri;
    private File profileheadTemp;

    private final int UPDATE_PORTRAIT_OK = 0;
    private final int UPDATE_PORTRAIT_FAIL = 1;
    private final int HANDLER_MSG_DELAY = 1000; //1 second

    private Dialog mLoadingDialog;
    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_PORTRAIT_OK) {
                dismissDialog();
                initData();
            } else if (msg.what == UPDATE_PORTRAIT_FAIL) {
                dismissDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.err_location_update_fail), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private AsyncTaskListener mUpdateListener = new AsyncTaskListener() {
        @Override
        public void onSuccess() {
            Message msg = Message.obtain();
            msg.what = UPDATE_PORTRAIT_OK;
            mUpdateHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }

        @Override
        public void onFailed(AVException e) {
            Message msg = Message.obtain();
            msg.what = UPDATE_PORTRAIT_FAIL;
            mUpdateHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_information);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();

        if (CommonUtils.hasSdcard()) {
            profileheadTemp = new File(Environment.getExternalStorageDirectory(), "/portrait.jpg");
            if(!profileheadTemp.exists()) {
                headUri = Uri.fromFile(profileheadTemp);
            }
        }
    }

    public void initView() {
        mTvTitle = (TextView) findViewById(R.id.title_name);
        mTvTitle.setOnClickListener(this);

        mVPortrait = (View) findViewById(R.id.detail_portrait);
        mVPortrait.setOnClickListener(this);
        mMyPortrait = (PortraitView) mVPortrait.findViewById(R.id.iv_portrait);
        mMyPortrait.setOnClickListener(this);

        mVUserName = (View) findViewById(R.id.detail_nick_name);
        mVUserName.setOnClickListener(this);
        mTvUserName = (TextView) findViewById(R.id.tv_nick_name_infor);

        mVMobileNum = (View) findViewById(R.id.detail_mobile);
        mVMobileNum.setOnClickListener(this);
        mTvMobile = (TextView) findViewById(R.id.tv_mobile_infor);

        mVBirthday = (View) findViewById(R.id.detail_birth);
        mVBirthday.setOnClickListener(this);
        mTvBirth = (TextView) findViewById(R.id.tv_birth_infor);

        mVGendle = (View) findViewById(R.id.detail_gendle);
        mVGendle.setOnClickListener(this);
        mTvGendle = (TextView) findViewById(R.id.tv_gendle_infor);

        mVSignature = (View) findViewById(R.id.detail_signature);
        mVSignature.setOnClickListener(this);
        mTvSignature = (TextView) findViewById(R.id.tv_signature_infor);

        mVLocation = (View) findViewById(R.id.detail_location);
        mVLocation.setOnClickListener(this);
        mTvLocation = (TextView) findViewById(R.id.tv_location_infor);
    }

    public void initData() {
        //TODO: we need a API to get all current user data.
        myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if (myProfileInfo != null) {
            mMyPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(), myProfileInfo.getUsrName().substring(0, 1), -1);
            mTvUserName.setText(myProfileInfo.getUsrName());
            mTvMobile.setText(myProfileInfo.getMobileNum());
            mTvSignature.setText(myProfileInfo.getSignature());

            if(myProfileInfo.getLocation() != null){
                mTvLocation.setText(myProfileInfo.getLocation().getAddrInfor());
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_name://back
                setResult(AppConstant.ACTIVITY_RETURN_CODE_CANCEL);
                finish();
                break;

            case R.id.detail_portrait:
                logger.d("onClick, portrait, select pic.");
                startImageSelectActivity();
                break;

            case R.id.iv_portrait:
                logger.d("onClick, portrait, browser pic.");
                if (myProfileInfo != null) {
                    startImageBrowserActivity(myProfileInfo.getPortraitPath());
                }
                break;

            case R.id.detail_nick_name:
                logger.d("onClick, user name.");
                startEditActivity(ProfileUtil.REG_TYPE_USER_NAME);
                break;

            case R.id.detail_mobile:
                logger.d("onClick, mobile.");

                break;

            case R.id.detail_gendle:
                logger.d("onClick, gendle.");

                break;

            case R.id.detail_birth:
                logger.d("onClick, birth.");
                break;

            case R.id.detail_signature:
                logger.d("onClick, signature.");
                startEditActivity(ProfileUtil.REG_TYPE_SIGNATURE);
                break;

            case R.id.detail_location:
                logger.d("onClick, signature.");
                startLocationActivity();
                break;

            default:

                break;
        }
    }

    private void startEditActivity(int editType) {
        Intent intent = new Intent(DetailInformationActivity.this, EditStrProfileInfoActivity.class);
        intent.putExtra(ProfileUtil.EDIT_ACTIVITY_START_TYPE, editType);
        startActivityForResult(intent, REQUEST_CODE_EDIT_STR);
    }

    private void startImageSelectActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ImageChooseActivity.class);
        intent.putExtra(ImageChooseActivity.HAVE_CHOSEN_NUM, 0);
        intent.putExtra(ImageChooseActivity.IMAGE_COUNT, 1);
        intent.putExtra(ImageChooseActivity.IS_FULL_SIZE_OPTION, false);

        intent.putExtra(ImageChooseActivity.IMAGE_CHOOSE_TYPE, ImageChooseActivity.FROM_MESSAGE_CODE);
        startActivityForResult(intent, REQUEST_CODE_PICTURE_CHOOSE);

        //REQUEST_CODE_PICTURE_CHOOSE
        //startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICTURE_CHOOSE) {
            if (resultCode != RESULT_OK) {
                return;
            }

            String imagePath = null;
            ArrayList<String> pathList = data.getExtras()
                    .getStringArrayList(ImageBrowserActivity.INTENT_CHOSE_PATHLIST);

            if (pathList != null && pathList.size() > 0) {
                imagePath = pathList.get(0);
                if (!TextUtils.isEmpty(imagePath)) {
                    File fPic = new File(imagePath);
                    if (fPic != null && fPic.exists()) {
                        crop(Uri.fromFile(fPic));
                    } else {
                        Toast.makeText(this, getString(R.string.err_file_donot_exist), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_PICTURE_CUT) {
            //if user select cancel, the resule will be 0;
            if (resultCode == 0 || headUri == null || TextUtils.isEmpty(headUri.toString())) {
                return;
            }
            //TODO: should add listener on update portrait method!!!
            showDialog();
            ProfileManager.getInstance().updatePortrait(headUri.getPath(), mUpdateListener);

            if (profileheadTemp != null && profileheadTemp.exists()) {
                profileheadTemp.delete();
                headUri = null;
            }
        } else if(requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode != RESULT_OK || data == null) {
                return;
            }

            GeoData location = new GeoData(data.getDoubleExtra(LocationActivity.LATITUDE, 0),
                    data.getDoubleExtra(LocationActivity.LONGITUDE, 0),
                    data.getStringExtra(LocationActivity.ADDRESS));

            ProfileManager.getInstance().updateUserLocation(location, mUpdateListener);
        } else if(requestCode == REQUEST_CODE_EDIT_STR) {
            if (resultCode != RESULT_OK) {
                return;
            }
        }
    }

    private void startImageBrowserActivity(String imageUrl) {
        Intent intent = new Intent(this, ImageBrowserActivity.class);
        ArrayList<String> originFilePath = new ArrayList<String>();
        originFilePath.add(imageUrl);
        intent.putExtra(ImageBrowserActivity.INTENT_ORIGIN_PATHLIST, originFilePath);
        intent.putExtra(ImageBrowserActivity.INTENT_CHOSE_PATHLIST, originFilePath);
        intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_FROM_TYPE, ImageBrowserActivity._TYPE_FROM_CAMERA);
        intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_MAX_NUM, 1);
        intent.putExtra(ImageBrowserActivity.INTENT_DISABLE_OK_BTN, true);
        startActivity(intent);
    }

    /**
     * @param uri 图片路径(裁剪头像以后如果回传data某些机型会内存溢出，通过URI回传)
     * @方法名：crop
     * @描述：剪切图片
     * @输出：void
     * @作者：Administrator
     */
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        // 图片格式
        intent.putExtra("noFaceDetection", false);// 取消人脸识别
        intent.putExtra("return-data", false);// true:不返回URI，false：返回URI
        intent.putExtra(MediaStore.EXTRA_OUTPUT, headUri);
        intent.putExtra("scale", true);// 黑边
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        // intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, REQUEST_CODE_PICTURE_CUT);
    }

    /**
     * 读取URI所在的图片
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context mContext) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, getString(R.string.common_please_wait_for_a_moment),
                    true, null);
            mLoadingDialog.show();
        }
    }

    private void dismissDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    private void startLocationActivity() {
        Intent intent = new Intent(this, LocationActivity.class);

        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }
}
