package com.example.walkarround.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
    TextView mTitle = null;

    private final int NICK_MAX_LENGTH = 32;

    //Request code
    private final int REQUEST_CODE_NEXT_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_input_nickname);

        mEtNickName = (EditText) findViewById(R.id.nick_edit);
        mTitle = (TextView) findViewById(R.id.title_name);
        mBtNext = (Button) findViewById(R.id.btn_nextstep);

        mEtNickName.setOnClickListener(this);
        mTitle.setOnClickListener(this);
        mBtNext.setOnClickListener(this);

        //Init nick name
        initNickName();
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
                Intent intent = new Intent(this, PhoneAndPasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEXT_PAGE);
            }
        } else if(view.getId() == R.id.title_name) {
            this.finish();
        }
    }

    private void initNickName() {
        String strNick = LoginManager.getInstance().getUserName();
        if (!TextUtils.isEmpty(strNick) && mEtNickName != null) {
            mEtNickName.setText(strNick);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_PAGE) {
            if (resultCode == PhoneAndPasswordActivity.RESULT_OK) {
                setResult(CommonUtils.ACTIVITY_FINISH_NORMAL_FINISH);
                this.finish();
            } else if(resultCode == PhoneAndPasswordActivity.RESULT_BACK) {
                initNickName();
            }
        }
    }
}
