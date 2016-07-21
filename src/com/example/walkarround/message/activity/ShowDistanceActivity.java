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
import com.example.walkarround.base.view.RippleView;
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
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.setting_title);

        mSearchingView = (RippleView)findViewById(R.id.searchingView);

        mRlSearchArea = (RelativeLayout)findViewById(R.id.rlSearching);
    }

    private void initData() {
        Intent intent = getIntent();
        if(intent != null) {
            long threadId = intent.getLongExtra(PARAMS_THREAD_ID, 0l);
            MessageSessionBaseModel conversation = WalkArroundMsgManager.getInstance(getApplicationContext()).getSessionByThreadId(threadId);
            int colorIndex = conversation.colorIndex;
            if(colorIndex >= 0) {
                //mRlSearchArea.setBackgroundColor(MessageUtil.getFriendColor(colorIndex));
                mSearchingView.setInitColor(MessageUtil.getFriendColor(colorIndex));
                //mSearchingView.setInitAlphaValue(100);
                logger.d("init  color index is :" + colorIndex);
            }

            conversation.getContact();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl:
                finish();
                break;
            default:
                break;
        }
    }
}
