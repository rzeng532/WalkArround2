/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.RippleView;

/**
 * TODO: description
 * Date: 2016-05-19
 *
 * @author Administrator
 */
public class TestActivity extends Activity {

    private RippleView shoopView;
    private ImageView roundedImageView;
//    private SoundPool soundPool;
//    private int music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RippleView", " TestActivity onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        Log.d("RippleView", " TestActivity initView.");

        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        //soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        // 把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
        //music = soundPool.load(this, R.raw.shoop, 1);
        shoopView = (RippleView) findViewById(R.id.RippleView);
        roundedImageView = (ImageView) findViewById(R.id.my_photo);
//        roundedImageView.setVisibility(View.INVISIBLE);
//        shoopView.start();
        roundedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoopView.isStarting()) {
                    Log.d("RippleView", " TestActivity shoopView.stop.");
                    //如果动画正在运行就停止，否则就继续执行
                    shoopView.stop();
                } else {
                    // 执行动画
                    Log.d("RippleView", " TestActivity shoopView.start.");
                    shoopView.start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shoopView.isStarting()) {
            //如果动画正在运行就停止，否则就继续执行
            shoopView.stop();
        }
    }
}
