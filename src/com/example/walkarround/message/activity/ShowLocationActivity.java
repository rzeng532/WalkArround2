package com.example.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.avos.avoscloud.AVException;
import com.example.walkarround.Location.activity.LocationActivity;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.GoTogetherTask;
import com.example.walkarround.main.task.QuerySpeedDateIdTask;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

public class ShowLocationActivity extends Activity implements View.OnClickListener {

    private MapView mapView;
    private TextView mTvDetailInfor;
    private TextView mTvFullInfor;
    private TextView mTvSelectPosition;
    private TextView mTvAcceptPosition;
    private ImageView mIvLocateIcon;
    private boolean mBUserSelect = false;
    private AMap aMap;
    private Marker mCurMarker;
    private Marker mTargetMarker;

    private static final Logger logger = Logger.getLogger(ShowLocationActivity.class.getSimpleName());

    private Dialog mLoadingDialog;
    private static final int MSG_AGREE_TO_WALKARROUND_SUC = 1;
    private static final int MSG_AGREE_TO_WALKARROUND_FAIL = 2;

    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {

            dismissCircleDialog();
            switch (msg.what) {
                case MSG_AGREE_TO_WALKARROUND_SUC:
                    //Send I agree to walk arround.
                    //Use RESULT_FIRST_USER as agreement for prior activity.
                    setResult(RESULT_FIRST_USER);
                    finish();
                    break;
                case MSG_AGREE_TO_WALKARROUND_FAIL:
                    Toast.makeText(ShowLocationActivity.this, R.string.msg_agree_to_walkarround_fail, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    AsyncTaskListener mMyPositionListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            GeoData geoData = LocationManager.getInstance(getApplicationContext()).getCurrentLoc();

            if(geoData != null) {
                mCurMarker.setPosition(new LatLng(geoData.getLatitude(), geoData.getLongitude()));
                if(mBUserSelect) {
                    mBUserSelect = false;
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(geoData.getLatitude(), geoData.getLongitude())));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                }
            }
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
        }
    };

    //Listener for go together API.
    private HttpTaskBase.onResultListener mGoTogetherListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_GO_TOGETHER)) {
                //Get status & Get TO user.
                logger.d("go together response success: \r\n" + (String) object);
                mUIHandler.sendEmptyMessage(MSG_AGREE_TO_WALKARROUND_SUC);
            } else {
                logger.d("go together response failed");
                mUIHandler.sendEmptyMessage(MSG_AGREE_TO_WALKARROUND_FAIL);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private HttpTaskBase.onResultListener mGetSpeedIdTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE)) {
                //Get status & Get TO user.
                String strSpeedDateId = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID);
                if(!TextUtils.isEmpty(strSpeedDateId)) {
                    logger.d("Query speed date id response success: " + strSpeedDateId);
                    ThreadPoolManager.getPoolManager().addAsyncTask(new GoTogetherTask(getApplicationContext(),
                            mGoTogetherListener,
                            HttpUtil.HTTP_FUNC_GO_TOGETHER,
                            HttpUtil.HTTP_TASK_GO_TOGETHER,
                            GoTogetherTask.getParams(strSpeedDateId),
                            TaskUtil.getTaskHeader()));
                }
            } else {
                logger.d("Query speed date id failed");
                mUIHandler.sendEmptyMessage(MSG_AGREE_TO_WALKARROUND_FAIL);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        initView();
        mapView.onCreate(savedInstanceState);
        initMap();
        LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_SHOW_LOCATION, mMyPositionListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    void initView() {
        mapView = (MapView) findViewById(R.id.map);

        //Detail information
        mTvDetailInfor = (TextView)findViewById(R.id.detail);
        mTvFullInfor = (TextView)findViewById(R.id.full_infor);

        //Location icon
        mIvLocateIcon = (ImageView)findViewById(R.id.locate_iv);
        mIvLocateIcon.setOnClickListener(this);

        //Buttons
        int sender = getIntent().getIntExtra(LocationActivity.SENDER_OR_RECEIVER, 0);
        if(sender == MessageConstant.MessageSendReceive.MSG_RECEIVE) {
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
            mTvSelectPosition = (TextView)findViewById(R.id.select_another);
            mTvSelectPosition.setOnClickListener(this);
            mTvAcceptPosition = (TextView)findViewById(R.id.accept_place);
            mTvAcceptPosition.setOnClickListener(this);
        } else {
            findViewById(R.id.button_layout).setVisibility(View.GONE);
        }
    }

    void initMap() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.msg_select_place_title);

        aMap = mapView.getMap();
        Intent intent = getIntent();
        LatLng latLng = new LatLng(intent.getDoubleExtra(LocationActivity.LATITUDE, 0),
                intent.getDoubleExtra(LocationActivity.LONGITUDE, 0));
        String[] address = intent.getStringExtra(LocationActivity.ADDRESS).split(MessageUtil.MAP_DETAIL_INFOR_SPLIT);
        String addressInfo = null;
        if(address.length > 1) {
            addressInfo = address[1];
        }

        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.message_icon_map_position));
        markerOptions.title(addressInfo);
        mTargetMarker = aMap.addMarker(markerOptions);
        mTargetMarker.setPosition(latLng);

        MarkerOptions curMarkerOptions = new MarkerOptions();
        curMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_position));
        mCurMarker = aMap.addMarker(curMarkerOptions);

        //Init detail information
        mTvDetailInfor.setText(address[0]);
        mTvFullInfor.setText(addressInfo);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back_rl) {
            setResult(RESULT_CANCELED);
            finish();
        } else if(v.getId() == R.id.locate_iv) {
            mBUserSelect = true;
            LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN, mMyPositionListener);
        } else if(v.getId() == R.id.select_another) {
            setResult(RESULT_OK);
            finish();
        } else if(v.getId() == R.id.accept_place) {
            //Get speed data id && go together.
            getSpeedDataId();
        }
    }

    private void getSpeedDataId() {
        String userObjId = ProfileManager.getInstance().getCurUsrObjId();

        if(TextUtils.isEmpty(userObjId)) {
            return;
        }

        showCircleDialog();

        ThreadPoolManager.getPoolManager().addAsyncTask(new QuerySpeedDateIdTask(getApplicationContext(),
                mGetSpeedIdTaskListener,
                HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE,
                HttpUtil.HTTP_TASK_QUERY_SPEED_DATE,
                QuerySpeedDateIdTask.getParams(userObjId),
                TaskUtil.getTaskHeader()));
    }

    private void showCircleDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, true, null);
        }
        logger.d("Show dialog.");
        mLoadingDialog.show();
    }

    private void dismissCircleDialog() {
        logger.d("Dismiss dialog.");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
