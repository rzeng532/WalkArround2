/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.base.view.RippleView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.util.Logger;

/**
 * TODO: description
 * Date: 2016-07-20
 *
 * @author Administrator
 */
public class ShowDistanceActivity extends Activity implements View.OnClickListener {

    private Logger logger = Logger.getLogger(ShowDistanceActivity.class.getSimpleName());
    private RippleView mSearchingView;
    private RelativeLayout mRlSearchArea;
    private PortraitView mPvFriend;
    private TextView mTvPleaseClickPortrait;

    public static final String PARAMS_THREAD_ID = "thread_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_show_distance);

        logger.d("onCreate");

        initView();

        initData();

        mSearchingView.start();
    }

    private void initView() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView) (title.findViewById(R.id.display_name))).setText(R.string.setting_title);

        mSearchingView = (RippleView) findViewById(R.id.searchingView);
        mRlSearchArea = (RelativeLayout) findViewById(R.id.rlSearching);
        mPvFriend = (PortraitView) findViewById(R.id.pv_friend_portrait);
        mPvFriend.setOnClickListener(this);
        mTvPleaseClickPortrait = (TextView)findViewById(R.id.tv_click_portrait);

        //setPortraitViewVisible(View.GONE);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            //Get conversation color, default is color index 1.
            long threadId = intent.getLongExtra(PARAMS_THREAD_ID, 0l);
            MessageSessionBaseModel conversation = WalkArroundMsgManager.getInstance(getApplicationContext()).getSessionByThreadId(threadId);
            int colorIndex = conversation.colorIndex;
            int color = MessageUtil.getFriendColor(colorIndex);
            //Init background color
            if (color >= 0) {
                //Activity body color
                mRlSearchArea.setBackgroundColor(getResources().getColor(color));

                //Title color
                RelativeLayout rlTitle = (RelativeLayout) (findViewById(R.id.title));
                if (rlTitle != null) {
                    rlTitle.setBackgroundColor(getResources().getColor(color));
                }

                logger.d("init  color index is :" + colorIndex);
            }

            //Set searching circle color
            mSearchingView.setInitColor(R.color.bgcor15);

            //Init portrait
            String usrObjId = conversation.getContact();
            ContactInfo usr = ContactsManager.getInstance(this.getApplicationContext()).getContactByUsrObjId(usrObjId);
            if (usr != null) {
                mPvFriend.setBaseData(usr.getUsername(), usr.getPortrait().getUrl(),
                        usr.getUsername().substring(0, 1), -1);

                //Init bottom indication text
                mTvPleaseClickPortrait.setText(getString(R.string.walk_rule_please_click_portrait, usr.getUsername()));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl:
                finish();
                break;
            case R.id.pv_friend_portrait:
                //finish();
                break;
            default:
                break;
        }
    }

    /*
     * True: visible
     * False: gone
     */
    private void setPortraitViewVisible(int visibility) {
        if(mPvFriend != null) {
            mPvFriend.setVisibility(visibility);
        }

        if(mTvPleaseClickPortrait != null) {
            mTvPleaseClickPortrait.setVisibility(visibility);
        }
    }
}
