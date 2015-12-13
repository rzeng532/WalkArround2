package com.example.walkarround.myself.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.walkarround.myself.model.MyProfileInfo;
import org.w3c.dom.Text;

import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppSharedPreference;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.image.ImageBrowserActivity;
import com.example.walkarround.util.image.ImageChooseActivity;

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

    private View mVPortrait;
    private View mVUserName;
    private View mVMobileNum;
    private View mVBirthday;
    private View mVGendle;
    private View mVSignature;

    private final int REQUEST_CODE_PICTURE_CHOOSE = 100;
    private final int REQUEST_CODE_PICTURE_CUT = 101;

    private Logger logger = Logger.getLogger(DetailInformationActivity.class.getSimpleName());
    private MyProfileInfo myProfileInfo = null;
    Uri headUri;
    protected File profileheadTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_information);
        initView();
        //initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
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
    }

    public void initData() {
        //TODO: we need a API to get all current user data.
        myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if(myProfileInfo != null) {
            mMyPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(), myProfileInfo.getUsrName().substring(0, 1), -1);
            mTvUserName.setText(myProfileInfo.getUsrName());
            mTvMobile.setText(myProfileInfo.getMobileNum());
            mTvSignature.setText(myProfileInfo.getSignature());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_name://back
                finish();
                break;

            case R.id.detail_portrait:
                logger.d("onClick, portrait");
                startImageSelectActivity();
                break;

            case R.id.iv_portrait:
                logger.d("onClick, portrait");
                if(myProfileInfo != null) {
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
            default:

                break;
        }
    }

    private void startEditActivity(int editType) {
        Intent intent = new Intent(DetailInformationActivity.this, EditStrProfileInfoActivity.class);
        intent.putExtra(ProfileUtil.EDIT_ACTIVITY_START_TYPE, editType);
        startActivity(intent);
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
        if (requestCode == REQUEST_CODE_PICTURE_CHOOSE) {
            if (resultCode != RESULT_OK) {
                return;
            }

            String imagePath = null;
            ArrayList<String> pathList = data.getExtras()
                    .getStringArrayList(ImageBrowserActivity.INTENT_CHOSE_PATHLIST);

            if (pathList != null && pathList.size() > 0) {
                imagePath = pathList.get(0);
                if(!TextUtils.isEmpty(imagePath)) {
                    //TODO: should add listener on update portrait method!!!
                    //crop(Uri.parse(imagePath));
                    ProfileManager.getInstance().updatePortrait(imagePath);
                    mMyPortrait.setBaseData(null, imagePath, null, -1);
                }
            }
        } else if(requestCode == REQUEST_CODE_PICTURE_CUT) {

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
     * @param uri
     *            图片路径(裁剪头像以后如果回传data某些机型会内存溢出，通过URI回传)
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
}
