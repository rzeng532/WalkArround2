package com.awalk.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
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
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.awalk.walkarround.Location.activity.LocationActivity;
import com.awalk.walkarround.Location.manager.LocationManager;
import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.main.parser.WalkArroundJsonResultParser;
import com.awalk.walkarround.main.task.GoTogetherTask;
import com.awalk.walkarround.base.task.TaskUtil;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.util.MessageConstant;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.NaviMapUtil;
import com.awalk.walkarround.util.http.HttpTaskBase;
import com.awalk.walkarround.util.http.HttpUtil;
import com.awalk.walkarround.util.http.ThreadPoolManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ShowLocationActivity extends Activity implements View.OnClickListener {

    private MapView mapView;
    private TextView mTvDetailInfor;
    private TextView mTvFullInfor;
    private TextView mTvSelectPosition;
    private TextView mTvAcceptPosition;
    private TextView mTvNavigation;
    private ImageView mIvLocateIcon;
    private boolean mBUserSelect = false;
    private AMap aMap;
    private Marker mCurMarker = null;
    private Marker mTargetMarker;
    private int mCurThreadStatus = MessageUtil.WalkArroundState.STATE_INIT;
    private LatLng mMapData;
    private String mMapGeoDetail;

    private static final Logger logger = Logger.getLogger(ShowLocationActivity.class.getSimpleName());

    private Dialog mLoadingDialog;
    private static final int MSG_AGREE_TO_WALKARROUND_SUC = 1;
    private static final int MSG_AGREE_TO_WALKARROUND_FAIL = 2;

    private final int ZOOM_SIZE = 15;

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

            if (geoData != null) {
                mTvNavigation.setEnabled(true);

                mCurMarker.setPosition(new LatLng(geoData.getLatitude(), geoData.getLongitude()));
                if (mBUserSelect) {
                    mBUserSelect = false;
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(geoData.getLatitude(), geoData.getLongitude())));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_SIZE));
                }
            }
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
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
                String strSpeedDateId = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID);
                if (!TextUtils.isEmpty(strSpeedDateId)) {
                    ProfileManager.getInstance().getMyProfile().setSpeedDateId(strSpeedDateId);

                    ThreadPoolManager.getPoolManager().addAsyncTask(new GoTogetherTask(getApplicationContext(),
                            mGoTogetherListener,
                            HttpUtil.HTTP_FUNC_GO_TOGETHER,
                            HttpUtil.HTTP_TASK_GO_TOGETHER,
                            GoTogetherTask.getParams(strSpeedDateId),
                            TaskUtil.getTaskHeader()));
                } else {
                    mUIHandler.sendEmptyMessageDelayed(MSG_AGREE_TO_WALKARROUND_FAIL, 1000);
                    logger.d("Get speed date id OK but data is EMPTY.");
                }
            } else {
                mUIHandler.sendEmptyMessageDelayed(MSG_AGREE_TO_WALKARROUND_FAIL, 1000);
                logger.d("Failed to get speed date id!!!");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

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
        AVAnalytics.onResume(this);
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
        AVAnalytics.onPause(this);
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
        mTvDetailInfor = (TextView) findViewById(R.id.detail);
        mTvFullInfor = (TextView) findViewById(R.id.full_infor);
        mTvNavigation = (TextView) findViewById(R.id.tv_navigation);
        mTvNavigation.setEnabled(false);
        mTvNavigation.setOnClickListener(this);

        //Location icon
        mIvLocateIcon = (ImageView) findViewById(R.id.locate_iv);
        mIvLocateIcon.setOnClickListener(this);

        //Buttons
        int sender = getIntent().getIntExtra(LocationActivity.SENDER_OR_RECEIVER, 0);
        if (sender == MessageConstant.MessageSendReceive.MSG_RECEIVE) {
            findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
            mTvSelectPosition = (TextView) findViewById(R.id.select_another);
            mTvSelectPosition.setOnClickListener(this);
            mTvAcceptPosition = (TextView) findViewById(R.id.accept_place);
            mTvAcceptPosition.setOnClickListener(this);
        } else {
            findViewById(R.id.button_layout).setVisibility(View.GONE);
        }

        mCurThreadStatus = 0;

        long threadId = getIntent().getLongExtra(LocationActivity.THREAD_ID, -1);
        if (threadId != -1) {
            mCurThreadStatus = WalkArroundMsgManager.getInstance(WalkArroundApp.getInstance().getApplicationContext()).getConversationStatus(threadId);
        }
    }

    void initMap() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView) (title.findViewById(R.id.display_name))).setText(R.string.msg_select_place_title);

        aMap = mapView.getMap();
        Intent intent = getIntent();
        mMapData = new LatLng(intent.getDoubleExtra(LocationActivity.LATITUDE, 0),
                intent.getDoubleExtra(LocationActivity.LONGITUDE, 0));
        String[] address = intent.getStringExtra(LocationActivity.ADDRESS).split(MessageUtil.MAP_DETAIL_INFOR_SPLIT);
        //String addressInfo = null;
        if (address.length > 1) {
            mMapGeoDetail = address[0];
        }

        aMap.moveCamera(CameraUpdateFactory.changeLatLng(mMapData));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_SIZE));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.message_icon_map_position));
        markerOptions.title(mMapGeoDetail);
        mTargetMarker = aMap.addMarker(markerOptions);
        mTargetMarker.setPosition(mMapData);

        MarkerOptions curMarkerOptions = new MarkerOptions();
        curMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_position));
        mCurMarker = aMap.addMarker(curMarkerOptions);

        //Init detail information
        mTvDetailInfor.setText(address[0]);
        mTvFullInfor.setText(mMapGeoDetail);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_rl) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (v.getId() == R.id.locate_iv) {
            mBUserSelect = true;
            LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_SHOW_LOCATION_ONCLICK, mMyPositionListener);
        } else if (v.getId() == R.id.select_another) {
//            if (mCurThreadStatus == MessageUtil.WalkArroundState.STATE_WALK
//                    || mCurThreadStatus == MessageUtil.WalkArroundState.STATE_IMPRESSION) {
//                Toast.makeText(ShowLocationActivity.this, R.string.msg_already_on_walking_state, Toast.LENGTH_LONG).show();
//                return;
//            }
            setResult(RESULT_OK);
            finish();
        } else if (v.getId() == R.id.accept_place) {
            //Get speed data id & go together.
            String speedId = ProfileManager.getInstance().getSpeedDateId();
            if (mCurThreadStatus == MessageUtil.WalkArroundState.STATE_END) {
                //If we already be friend, we will skip some steps and send "I agreed" directly.
                showCircleDialog();
                mUIHandler.sendEmptyMessageDelayed(MSG_AGREE_TO_WALKARROUND_SUC, 1000);
            }
//            else if (mCurThreadStatus == MessageUtil.WalkArroundState.STATE_WALK
//                    || mCurThreadStatus == MessageUtil.WalkArroundState.STATE_IMPRESSION) {
//                Toast.makeText(ShowLocationActivity.this, R.string.msg_already_on_walking_state, Toast.LENGTH_LONG).show();
//            }
            else if (!TextUtils.isEmpty(speedId)) {
                ThreadPoolManager.getPoolManager().addAsyncTask(new GoTogetherTask(getApplicationContext(),
                        mGoTogetherListener,
                        HttpUtil.HTTP_FUNC_GO_TOGETHER,
                        HttpUtil.HTTP_TASK_GO_TOGETHER,
                        GoTogetherTask.getParams(speedId),
                        TaskUtil.getTaskHeader()));
            } else {
                //If speed date id is empty.
                mUIHandler.sendEmptyMessage(MSG_AGREE_TO_WALKARROUND_SUC);
            }
        } else if (v.getId() == R.id.tv_navigation) {
            if (mMapData != null) {
                //startSysMap(mMapData.latitude, mMapData.longitude);
                DialogFactory.getMappingDialog(ShowLocationActivity.this
                        , getString(R.string.mapping_navi_start_indicate)
                        , new DialogFactory.ConfirmDialogClickListener() {
                            @Override
                            public void onConfirmDialogConfirmClick() {
                                startNavi(mMapData.latitude, mMapData.longitude);
                            }
                        }).show();
            }
        }
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

    private boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    private void startSysMap(double lat, double lng) {

        double[] gps = NaviMapUtil.gcj02_To_Gps84(lat, lng);
        Uri mUri = Uri.parse("geo:" + gps[0] + "," + gps[1] + "?q=" + mMapGeoDetail);

        Intent intent = new Intent("android.intent.action.VIEW", mUri);
        startActivity(intent);
    }

    private void startNavi(double lat, double lng) {

        ArrayList<String> mapList = new ArrayList<>();
        final ArrayList<String> toastList = new ArrayList<>();
        final ArrayList<Intent> startList = new ArrayList<>();
        String curLat = null;
        String curLng = null;
        double[] gps = NaviMapUtil.gcj02_To_Gps84(lat, lng);
        Uri mUri = Uri.parse("geo:" + gps[0] + "," + gps[1] + "?q=" + mMapGeoDetail);
        Intent intent = new Intent("android.intent.action.VIEW", mUri);

        if (isAvilible(this, "com.baidu.BaiduMap")) {//传入指定应用包名
            mapList.add(getString(R.string.mapping_baidu));
            toastList.add(getString(R.string.mapping_start_baidu_navi));

            double[] bdgps = NaviMapUtil.gaoDeToBaidu(lat, lng);
            Intent intentBaidu = null;
            try {
                double[] mygps;
                String regionStr = "";
                GeoData myGeo = ProfileManager.getInstance().getMyProfile().getLocation();
                if(myGeo != null) {
                    mygps = NaviMapUtil.gaoDeToBaidu(myGeo.getLatitude(), myGeo.getLongitude());
                    regionStr = "origin=" + mygps[0] + "," + mygps[1] + "&";
                }

                intentBaidu = Intent.getIntent("intent://map/direction?" + regionStr + "destination=latlng:" + bdgps[0] + "," + bdgps[1] + "|name:" + mMapGeoDetail + "&mode=walking&src="+mMapGeoDetail+"#Intent;" + "scheme=bdapp;package=com.baidu.BaiduMap;end");

            } catch (Exception e) {
                e.printStackTrace();
            }

//            Intent intentBaidu = NaviMapUtil.getBaiduIntent(this, bdgps[1] + "", bdgps[0] + "", null, null);
//            intentBaidu.setData(Uri.parse("baidumap://map/walknavi?"//origin=
//                                //+ "40.057406655722,116.2964407172"
//                                + "destination="
//                                + bdgps[1] + "," + bdgps[0]));
            startList.add(intentBaidu);
        }

        if(isAvilible(this, "com.autonavi.minimap")) {
            mapList.add(getString(R.string.mapping_gaode));
            toastList.add(getString(R.string.mapping_start_gaode_navi));

            Intent intentMini = null;
            //NaviMapUtil.getGaodeIntent(this, lng + "", lat + "");
            try {
                intentMini = Intent.getIntent("androidamap://route?sourceApplication=softname"
                        //+ "&slat="LATITUDE_A + "&slon="+ LONGTITUDE_A + "&sname="+"万家丽国际Mall"
                        + "&dlat=" + lat + "&dlon=" + lng + "&dname=" + mMapGeoDetail + "&dev=0&m=0&t=2");
                intent.setPackage("com.autonavi.minimap");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            startList.add(intentMini);
        }

        if (mapList.size() <= 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.mapping_there_is_no_navi), Toast.LENGTH_LONG).show();

            return;
        }
//        else {
//            Toast.makeText(getApplicationContext(), toastList.get(0), Toast.LENGTH_LONG).show();
//            startActivity(intent);
//
//            return;
//        }
        else {
            DialogFactory.showNaviListDialog(this, "", mapList
                    , new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(ShowLocationActivity.this, toastList.get(position), Toast.LENGTH_LONG).show();
                            startActivity(startList.get(position));
                        }
                    }).show();
        }
    }
}
