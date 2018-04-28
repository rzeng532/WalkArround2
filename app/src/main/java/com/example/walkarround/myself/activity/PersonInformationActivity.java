package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.example.walkarround.R;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.image.ImageLoaderManager;

/**
 * Created by Richard on 2017/10/11.
 */

public class PersonInformationActivity extends Activity implements View.OnClickListener  {

    private ImageView mIvPortrait;
    private TextView mTvNameAndAge;
    private TextView mTvImpressionpoint;
    private TextView mTvSignature;

    private String mName;
    private String mGender;
    private String mAge;
    private String mSignature;
    private String mPortraitUrl;

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
        TextView titleTv = (TextView)(findViewById(R.id.title).findViewById(R.id.display_name));
        titleTv.setText(R.string.profile_activity_title);

        if(mUsrObjId != null
                && mUsrObjId.equalsIgnoreCase(ProfileManager.getInstance().getCurUsrObjId())) {
            findViewById(R.id.title).findViewById(R.id.more_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.title).findViewById(R.id.more_rl).setOnClickListener(this);
        } else {
            findViewById(R.id.title).findViewById(R.id.more_rl).setVisibility(View.GONE);
        }

        //UI elements
        mIvPortrait = (ImageView) findViewById(R.id.iv_portrait);
        mTvNameAndAge = (TextView) findViewById(R.id.tv_infor);
        mTvImpressionpoint = (TextView) findViewById(R.id.tv_point);
        mTvSignature = (TextView) findViewById(R.id.tv_signature);
    }

    private void updateUIViaData() {
        if(!TextUtils.isEmpty(mPortraitUrl)) {
            ImageLoaderManager.displayImage(mPortraitUrl, -1, mIvPortrait);
        }

        String nameAndAge = "";
        if(!TextUtils.isEmpty(mName)) {
            nameAndAge = nameAndAge + mName;
        }
        if(!TextUtils.isEmpty(mAge)) {
            nameAndAge = nameAndAge + " , " + mAge;
        }
        if(!TextUtils.isEmpty(mGender)) {
            nameAndAge = nameAndAge + " , " + mGender;
        }

        mTvNameAndAge.setText(nameAndAge);

        mTvImpressionpoint.setVisibility(View.GONE);

        if(!TextUtils.isEmpty(mSignature)) {
            mTvSignature.setText(mSignature);
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
