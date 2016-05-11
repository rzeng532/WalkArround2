package com.example.walkarround.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.walkarround.R;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.util.CommonUtils;

/**
 * Created by Richard on 2015/11/22.
 */
public class NickNameActivity extends Activity implements View.OnClickListener{

    EditText mEtNickName = null;
    Button mBtNext = null;

    private final int NICK_MAX_LENGTH = 32;

    //Request code
    private final int REQUEST_CODE_NEXT_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_input_nickname);

        initView();

        //Init nick name
        initNickName();
    }

    private void initView() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.register_create_account);

        mEtNickName = (EditText) findViewById(R.id.nick_edit);
        mBtNext = (Button) findViewById(R.id.btn_nextstep);

        mEtNickName.setOnClickListener(this);
        mEtNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mEtNickName.getText().length() <= 0) {
                    mBtNext.setVisibility(View.GONE);
                } else {
                    mBtNext.setVisibility(View.VISIBLE);
                }
            }
        });

        mBtNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_nextstep) {
            String nickName = mEtNickName.getText().toString();
            //check inputting nickname
            if(TextUtils.isEmpty(nickName)) {
                //Cannot be empty
                Toast.makeText(this, R.string.login_nick_cannot_be_null, Toast.LENGTH_SHORT).show();
                return;
            } else if(nickName != null && nickName.length() > NICK_MAX_LENGTH) {
                //Cannot beyond max length
                Toast.makeText(this, R.string.login_nick_beyond_length_limit, Toast.LENGTH_SHORT).show();
                return;
            } else if(nickName != null && nickName.contains(" ")) {
                //Cannot contains space
                Toast.makeText(this, R.string.login_nick_cannot_contain_space, Toast.LENGTH_SHORT).show();
                return;
            } else {
                LoginManager.getInstance().setNickName(nickName);
                Intent intent = new Intent(this, SelectGenderActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEXT_PAGE);
            }
        } else if(view.getId() == R.id.back_rl) {
            //Clear all data if user select to exit
            LoginManager.getInstance().clearAllData();
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        LoginManager.getInstance().clearAllData();
        this.finish();
    }

    private void initNickName() {
        String strNick = LoginManager.getInstance().getUserName();
        if (!TextUtils.isEmpty(strNick) && mEtNickName != null) {
            mEtNickName.setText(strNick);
            mEtNickName.setSelection(strNick.length());
            mBtNext.setVisibility(View.VISIBLE);
        } else {
            mBtNext.setVisibility(View.GONE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_PAGE) {
            if (resultCode == CommonUtils.ACTIVITY_FINISH_NORMAL_FINISH) {
                setResult(CommonUtils.ACTIVITY_FINISH_NORMAL_FINISH);
                this.finish();
            } else if(resultCode == CommonUtils.ACTIVITY_FINISH_BACK) {
                initNickName();
            }
        }
    }
}
