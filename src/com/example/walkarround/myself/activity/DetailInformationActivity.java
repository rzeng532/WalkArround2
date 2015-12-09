package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.util.AppSharedPreference;
import com.example.walkarround.util.Logger;

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
    }

    public void initData() {
        //TODO: we need a API to get all current user data.
        String userName = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_USERNAME, "");
        String phoneNum = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
        String portraitPath = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PORTRAIT, "");

        mMyPortrait.setBaseData(userName, portraitPath, userName.substring(0, 1), -1);
        mTvUserName.setText(userName);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_name://back
                finish();
                break;

            case R.id.detail_portrait:
                logger.d("onClick, portrait");

                break;

            case R.id.detail_nick_name:
                logger.d("onClick, user name.");

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

                break;
            default:

                break;
        }
    }
}
