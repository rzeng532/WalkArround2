package com.example.walkarround.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.util.CommonUtils;

/**
 * Created by Richard on 2016/05/05
 * For user to select gender.
 */
public class SelectGenderActivity extends Activity implements View.OnClickListener{

    private Button mBtnMen;
    private Button mBtnFemale;

    //Request code
    private final int REQUEST_CODE_NEXT_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gender);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.register_create_account);

        mBtnMen = (Button) findViewById(R.id.btn_men);
        mBtnMen.setOnClickListener(this);
        mBtnFemale = (Button) findViewById(R.id.btn_female);
        mBtnFemale.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_female) {
            setUserGender(CommonUtils.PROFILE_GENDER_FEMALE);
            gotoNextPage();
        } else if(view.getId() == R.id.btn_men) {
            setUserGender(CommonUtils.PROFILE_GENDER_MEN);
            gotoNextPage();
        } else if(view.getId() == R.id.back_rl) {
            setResult(CommonUtils.ACTIVITY_FINISH_BACK);
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(CommonUtils.ACTIVITY_FINISH_BACK);
        this.finish();
    }

    private void setUserGender(String selected){
        LoginManager.getInstance().setGender(selected);
    }

    private void gotoNextPage(){
        Intent intent = new Intent(this, PhoneAndPasswordActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEXT_PAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_PAGE) {
            if (resultCode == PhoneAndPasswordActivity.RESULT_OK) {
                setResult(CommonUtils.ACTIVITY_FINISH_NORMAL_FINISH);
                this.finish();
            }
        }
    }
}
