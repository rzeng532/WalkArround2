/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.example.walkarround.R;
import com.example.walkarround.util.Logger;

/**
 * TODO: description
 * Date: 2016-04-26
 *
 * @author Administrator
 */
public class ConversationActivity extends Activity {

    private static final Logger mLogger = Logger.getLogger(ConversationActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        selectFragment();
        mLogger.d("ConversationActivity onCreate end.");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void selectFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.conversation_frame, ConversationFragment.getInstance());
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if(!ConversationFragment.getInstance().onBackPressed()) {
            this.finish();
        }
    }
}
