package com.example.walkarround.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;

/**
 * Created by Richard on 2017/10/17.
 */

public class UniversalWebView extends Activity implements View.OnClickListener {

    private WebView mWebView;
    //private ProgressBar pg;
    private String mShareUrl;

    private Dialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_universal_webview);
        initView();
        initData();
    }

    private void initView() {

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);

        mWebView = (WebView) findViewById(R.id.wb);
        // 启用javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new MyWebViewClient());

        mLoadingDialog = DialogFactory.getLoadingDialog(this, false, null);
    }

    private void initData() {
        mShareUrl = getIntent().getStringExtra("URL");

        mLoadingDialog.show();
        if(!TextUtils.isEmpty(mShareUrl)) {
            mWebView.loadUrl(mShareUrl);
        } else {
            mWebView.loadUrl("www.zouzou.com");
        }

        String title = getIntent().getStringExtra("TITLE");
        if(!TextUtils.isEmpty(title)) {
            ((TextView)(findViewById(R.id.title).findViewById(R.id.display_name))).setText(title);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.back_rl) {
            if (mWebView!= null && mWebView.canGoBack()) {
                mWebView.goBack();// 返回前一个页面
            } else {
                finish();
            }
        }
    }

    // 监听
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //在当前的webview中跳转到新的url
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            view.getSettings().setJavaScriptEnabled(true);
            mLoadingDialog.dismiss();
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            view.getSettings().setJavaScriptEnabled(true);

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            super.onReceivedError(view, errorCode, description, failingUrl);

        }
    }

    /**
     * 网页回退
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView!= null && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if(mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
