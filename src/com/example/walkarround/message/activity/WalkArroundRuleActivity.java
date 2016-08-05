/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.example.walkarround.R;

/**
 * TODO: description
 * Date: 2016-08-05
 *
 * @author Administrator
 */
public class WalkArroundRuleActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_rule);

        findViewById(R.id.iv_finish_icon).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_finish_icon:
                finish();
                break;
            default:
                break;
        }
    }
}
