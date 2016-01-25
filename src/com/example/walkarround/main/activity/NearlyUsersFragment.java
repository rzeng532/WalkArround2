package com.example.walkarround.main.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.avos.avoscloud.AVUser;
import com.example.walkarround.R;
import com.example.walkarround.flingswipe.SwipeFlingAdapterView;
import com.example.walkarround.main.adapter.NearlyUserListAdapter;
import com.example.walkarround.main.model.NearlyUser;
import com.example.walkarround.main.task.LikeSomeOneTask;
import com.example.walkarround.main.task.QueryNearlyUsers;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.radar.RadarScanView;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmcc on 16/1/11.
 */
public class NearlyUsersFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";
    private View mViewRoot;

    //For title and slide menu
    private View mTvTitle;

    //For nearly user list.
    private SwipeFlingAdapterView mUserFrame;
    //For buttons.
    private View mUserFrameButtons;

    //For radar display.
    private RadarScanView mRadarView;

    //Image mLeftDislike / mRightLike = dislike / like
    private ImageView mLeftDislike;
    private ImageView mRightLike;
    private String mStrFromUsrId;
    private String mStrToUsrId;

    //Real data from server.
    private static List<NearlyUser> mNearlyUserList = new ArrayList<>();

    //Private static instance for getInstance.
    private static NearlyUsersFragment mNUFragment;

    private NearlyUserListAdapter mUserListAdapter;

    private HttpTaskBase.onResultListener mLikeSomeoneListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {

        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private int i = 0;

    //Handler
    private final int RADAR_STOP_DELAY = 5 * 1000;
    private final int UPDATE_NEARLY_USERS = 0;
    private Handler mFragmentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_NEARLY_USERS) {
                showNearyUser();
            }
        }
    };

    public void updateNearlyUserList(List<NearlyUser> list) {

        if (getActivity() == null || getActivity().isDestroyed()) {
            return;
        }

        synchronized (NearlyUsersFragment.class) {
            mNearlyUserList.clear();
            mNearlyUserList.addAll(list);
            mStrToUsrId = mNearlyUserList.get(0).getObjectId();
        }

        mFragmentHandler.sendEmptyMessageDelayed(UPDATE_NEARLY_USERS, RADAR_STOP_DELAY);
    }

    public NearlyUsersFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static NearlyUsersFragment getInstance() {

        if(mNUFragment == null) {
            synchronized (NearlyUsersFragment.class) {
                if(mNUFragment == null) {
                    mNUFragment = new NearlyUsersFragment();
                    Bundle args = new Bundle();
                    args.putInt(NearlyUsersFragment.ARG_PLANET_NUMBER, 0);
                    mNUFragment.setArguments(args);
                }
            }
        }

        return mNUFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initView(inflater, container);
        initData(savedInstanceState);
        return mViewRoot;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title:
                //TODO: we should use handler for communication between activty and fragment later.
                DrawerLayout slideMenu = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                LinearLayout mViewLeftMenu = (LinearLayout) getActivity().findViewById(R.id.left_drawer);
                slideMenu.openDrawer(mViewLeftMenu);
                //getActivity().finish();
                break;

            default:
                break;
        }
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        mViewRoot = inflater.inflate(R.layout.fragment_planet, container, false);

        //Set those elements as gone at first and display them while there is data from server.
        mUserFrame = ((SwipeFlingAdapterView) mViewRoot.findViewById(R.id.userFrame));
        mUserFrame.setVisibility(View.GONE);
        mUserFrameButtons = ((View) mViewRoot.findViewById(R.id.userFrameButtons));
        mUserFrameButtons.setVisibility(View.GONE);

        //Like and Dislike button - init view & set onClicklistener.
        mLeftDislike = (ImageView)  mViewRoot.findViewById(R.id.left);
        mLeftDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                left();
            }
        });
        mRightLike = (ImageView)  mViewRoot.findViewById(R.id.right);
        mRightLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                right();
            }
        });

        //Init title
        mTvTitle = (View) mViewRoot.findViewById(R.id.title);
        mTvTitle.setOnClickListener(this);

        //Radar will be displayed at first.
        mRadarView = (RadarScanView) mViewRoot.findViewById(R.id.radar);
    }

    private void initData(Bundle savedInstanceState) {
        mUserListAdapter = new NearlyUserListAdapter(getActivity(), mNearlyUserList);

        mUserFrame.setAdapter(mUserListAdapter);
        mUserFrame.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                if(mNearlyUserList != null && mNearlyUserList.size() > 0) {
                    synchronized (NearlyUsersFragment.class) {
                        mStrToUsrId = mNearlyUserList.get(0).getObjectId();
                        mNearlyUserList.remove(0);
                    }
                    mUserListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onLeftCardExit(Object dataObject) {
                if (mNearlyUserList != null && mNearlyUserList.size() == 0) {
                    //If there is no data, display radar again.
                    showRadar();
                };
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                ThreadPoolManager.getPoolManager().addAsyncTask(new LikeSomeOneTask(getActivity().getApplicationContext(),
                        mLikeSomeoneListener,
                        HttpUtil.HTTP_FUNC_LIKE_SOMEONE,
                        HttpUtil.HTTP_TASK_LIKE_SOMEONE,
                        LikeSomeOneTask.getParams(mStrFromUsrId, mStrToUsrId),
                        TaskUtil.getTaskHeader()));

                if (mNearlyUserList != null && mNearlyUserList.size() == 0) {
                    //If there is no data, display radar again.
                    showRadar();
                };
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                //mNearlyUserList.add(new CardMode("循环测试", 18, list.get(itemsInAdapter % imageUrls.length - 1)));
                mUserListAdapter.notifyDataSetChanged();
                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                try {
                    View view = mUserFrame.getSelectedView();
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mUserFrame.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                ;
            }
        });

        mStrFromUsrId = AVUser.getCurrentUser().getObjectId();
    }

    /*
     * We will show radar on loading page / while there is no other data
     */
    private void showRadar() {
        mRadarView.setVisibility(View.VISIBLE);

        mUserFrameButtons.setVisibility(View.GONE);
        mUserFrame.setVisibility(View.GONE);
    }

    private void showNearyUser() {
        mRadarView.setVisibility(View.GONE);

        mUserFrame.setVisibility(View.VISIBLE);
        mUserFrameButtons.setVisibility(View.VISIBLE);

        if(mUserListAdapter != null) {
            mUserListAdapter.notifyDataSetChanged();
        }
    }

    private void right() {
        mUserFrame.getTopCardListener().selectRight();
    }

    private void left() {
        mUserFrame.getTopCardListener().selectLeft();
    }

}