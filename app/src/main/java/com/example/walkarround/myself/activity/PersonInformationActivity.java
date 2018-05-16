package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppConstant;

/**
 * Created by Richard on 2017/10/11.
 */

public class PersonInformationActivity extends Activity implements View.OnClickListener  {

    private PortraitView mIvPortrait;
    private PortraitView mIvSmallPortrait;
    private TextView mTvName;
    private TextView mTvAgeAndGender;
    private TextView mTvSignature;

    private String mName;
    private String mGender;
    private String mAge;
    private String mSignature;
    private String mPortraitUrl;
    private int mPortraitResId;

    private String mUsrObjId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_information);

        //Get user obj id.
        mUsrObjId = getIntent().getStringExtra(AppConstant.PARAM_USR_OBJ_ID);

        //Find view elements.
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);

        //Get latest user data.
        initData();

        //Update UI with data.
        updateUIViaData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);
    }

    private void initView() {
        //Title
        findViewById(R.id.title).findViewById(R.id.back_rl).setOnClickListener(this);
//        TextView titleTv = (TextView)(findViewById(R.id.title).findViewById(R.id.display_name));
//        titleTv.setText(R.string.profile_activity_title);
        findViewById(R.id.title).findViewById(R.id.line).setVisibility(View.GONE);
        if(mUsrObjId != null
                && mUsrObjId.equalsIgnoreCase(ProfileManager.getInstance().getCurUsrObjId())) {
            findViewById(R.id.title).findViewById(R.id.more_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.title).findViewById(R.id.more_rl).setOnClickListener(this);
        } else {
            findViewById(R.id.title).findViewById(R.id.more_rl).setVisibility(View.GONE);
        }

        //UI elements
        mIvPortrait = (PortraitView) findViewById(R.id.iv_portrait);
        mIvSmallPortrait = (PortraitView) findViewById(R.id.iv_small_portrait);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvAgeAndGender = (TextView) findViewById(R.id.tv_infor);
        mTvSignature = (TextView) findViewById(R.id.tv_signature);
    }

    private void updateUIViaData() {

        String nameAndAge = "";
        if(!TextUtils.isEmpty(mName)) {
            mTvName.setText(mName);
        }
        if(!TextUtils.isEmpty(mAge)) {
            nameAndAge = mAge;
        }
        if (!TextUtils.isEmpty(mGender)) {
            nameAndAge = TextUtils.isEmpty(nameAndAge) ? mGender : (nameAndAge + " , " + mGender);
        }

        mTvAgeAndGender.setText(nameAndAge);

        if(!TextUtils.isEmpty(mSignature)) {
            mTvSignature.setText(mSignature);
        }

//        if(!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mPortraitUrl)) {
        if (!TextUtils.isEmpty(mName)) {
            mIvPortrait.setBaseData(mName, mPortraitUrl,
                    mName.substring(0, 1), mPortraitResId);

            mIvSmallPortrait.setBaseData(mName, mPortraitUrl,
                    mName.substring(0, 1), mPortraitResId);
        }
    }

    private void initData() {

        if(!TextUtils.isEmpty(mUsrObjId)) {
            ContactInfo contact;
            if(mUsrObjId.equalsIgnoreCase(ProfileManager.getInstance().getCurUsrObjId())) {
                //Myself
                contact = ProfileManager.getInstance().getMyContactInfo();
            } else {
                //Friend
                contact = ContactsManager.getInstance(this).getContactByUsrObjId(mUsrObjId);
            }

            if(contact != null) {
                mName = contact.getUsername();
                if(TextUtils.isEmpty(mName)) {
                    mName = contact.getMobilePhoneNumber();
                }

                mAge = ProfileUtil.getAgeByBirth(contact.getBirthday());
                if (TextUtils.isEmpty(mAge)) {
                    mAge = getString(R.string.common_age_secret);
                }

                mGender = ProfileUtil.getGenderDisplayName(contact.getGender());

                mSignature = contact.getSignature();
                mPortraitUrl = (contact.getPortrait() != null ? contact.getPortrait().getUrl() : null);
                mPortraitResId = (contact.getPortrait() != null ? contact.getPortrait().getId() : -1);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.back_rl) {
            finish();
        } else if(view.getId() == R.id.more_rl) {
            startActivity(new Intent(this, DetailInformationActivity.class));
        }
    }
}
