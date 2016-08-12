/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PhotoView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.util.Logger;

/**
 * TODO: description
 * Date: 2016-08-12
 *
 * @author Administrator
 */
public class EvaluateActivity extends Activity implements View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    Logger myLogger = Logger.getLogger(EvaluateActivity.class.getSimpleName());

    public static final String PARAMS_FRIEND_OBJ_ID = "friend_obj_id";

    private TextView mTvDescription;
    private TextView mTvComplete;

    private RatingBar mRbHonest;
    private RatingBar mRbConversationStyle;
    private RatingBar mRbAppearance;
    private RatingBar mRbTemperament;

    private ContactInfo mFriend = null;
    private PhotoView mPvPortrait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);

        initData();

        initView();
    }

    private void initData() {
        //Get data from intent, like friend objId, speed data id.
        Intent intent = getIntent();
        if(intent != null) {
            String friendId = intent.getStringExtra(PARAMS_FRIEND_OBJ_ID);

            if(!TextUtils.isEmpty(friendId)) {
                mFriend = ContactsManager.getInstance(this).getContactByUsrObjId(friendId);
            }
        }
    }

    private void initView() {
        mTvDescription = (TextView)findViewById(R.id.tv_walk_description);
        mTvComplete = (TextView)findViewById(R.id.tv_complete_walk);

        mPvPortrait = (PhotoView)findViewById(R.id.pv_evaluate);

        mRbHonest = (RatingBar)findViewById(R.id.rating_honest);
        mRbHonest.setOnRatingBarChangeListener(this);

        mRbConversationStyle = (RatingBar)findViewById(R.id.rating_style_of_conversation);
        mRbConversationStyle.setOnRatingBarChangeListener(this);

        mRbAppearance = (RatingBar)findViewById(R.id.rating_appearance);
        mRbAppearance.setOnRatingBarChangeListener(this);

        mRbTemperament = (RatingBar)findViewById(R.id.rating_temperament);
        mRbTemperament.setOnRatingBarChangeListener(this);

        if(mFriend != null) {
            String friendName = mFriend.getUsername();
            mTvDescription.setText(getString(R.string.countdown_walk_with_who, friendName));

            mPvPortrait.setBaseData(friendName, mFriend.getPortrait().getUrl(), null,
                    R.drawable.contact_default_profile);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

        switch (ratingBar.getId()) {
            case R.id.rating_honest:
                myLogger.d("onRatingChanged rating_honest, value is " + rating);
                break;
            case R.id.rating_style_of_conversation:
                myLogger.d("onRatingChanged rating_style_of_conversation, value is " + rating);

                break;
            case R.id.rating_appearance:
                myLogger.d("onRatingChanged rating_appearance, value is " + rating);

                break;
            case R.id.rating_temperament:
                myLogger.d("onRatingChanged rating_temperament, value is " + rating);

                break;
            default:
                return;
        }
    }
}
