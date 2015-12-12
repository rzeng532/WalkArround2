package com.example.walkarround.myself.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

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

    private final int REQUEST_CODE_PICTURE_CHOOSE = 100;
    private final int REQUEST_CODE_PICTURE_CUT = 101;

    private Logger logger = Logger.getLogger(DetailInformationActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_information);
        initView();
        initData();
    }

    public void initView() {
        mTvTitle = (TextView) findViewById(R.id.title_name);
        mTvTitle.setOnClickListener(this);

        mVPortrait = (View) findViewById(R.id.detail_portrait);
        mVPortrait.setOnClickListener(this);
        mMyPortrait = (PortraitView) mVPortrait.findViewById(R.id.iv_portrait);

        mVUserName = (View) findViewById(R.id.detail_nick_name);
        mVUserName.setOnClickListener(this);
        mTvUserName = (TextView) findViewById(R.id.tv_nick_name_infor);

        mTvMobile = (TextView) findViewById(R.id.tv_mobile_infor);
        mTvMobile.setOnClickListener(this);

        mTvBirth = (TextView) findViewById(R.id.tv_birth_infor);
        mTvBirth.setOnClickListener(this);

        mTvGendle = (TextView) findViewById(R.id.tv_gendle_infor);
        mTvGendle.setOnClickListener(this);

        mTvSignature = (TextView) findViewById(R.id.tv_signature_infor);
        mTvSignature.setOnClickListener(this);
    }

    public void initData() {
        //TODO: we need a API to get all current user data.
        String userName = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_USERNAME, "");
        String phoneNum = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
        String portraitPath = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PORTRAIT, "");

        mMyPortrait.setBaseData(userName, portraitPath, userName.substring(0, 1), -1);
        mTvUserName.setText(userName);
        mTvMobile.setText(phoneNum);
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
        intent.putExtra(ImageChooseActivity.IMAGE_CHOOSE_TYPE, ImageChooseActivity.FROM_MESSAGE_CODE);
        startActivityForResult(intent, REQUEST_CODE_PICTURE_CHOOSE);

        //REQUEST_CODE_PICTURE_CHOOSE
        //startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICTURE_CHOOSE) {
            // 选择了要发送的图片
            if (resultCode != RESULT_OK) {
                return;
            }

            String imagePath = null;
            ArrayList<String> pathList = data.getExtras()
                    .getStringArrayList(ImageBrowserActivity.INTENT_CHOSE_PATHLIST);

            if (pathList != null && pathList.size() > 0) {
                imagePath = pathList.get(0);
                if(!TextUtils.isEmpty(imagePath)) {
                    ProfileManager.getInstance().updatePortrait(imagePath);
                    mMyPortrait.setBaseData(null, imagePath, null, -1);
                }
            }
        }
    }

}
