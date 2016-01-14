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
import com.example.walkarround.R;
import com.example.walkarround.main.model.NearlyUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by cmcc on 16/1/11.
 */
public class NearlyUsersFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";
    private View mViewRoot;
    private View mTvTitle;
    private ImageView mIvImage;
    private RadarScanView mRadarView;

    private static List<NearlyUser> mNearlyUserList;

    private static NearlyUsersFragment mNUFragment;

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
        if(mNearlyUserList != null) {
            mNearlyUserList.clear();
        } else {
            mNearlyUserList = new ArrayList<NearlyUser>();
        }

        mNearlyUserList.addAll(list);
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

        mIvImage = ((ImageView) mViewRoot.findViewById(R.id.image));
        mIvImage.setVisibility(View.GONE);
        mTvTitle = (View) mViewRoot.findViewById(R.id.title);
        mTvTitle.setOnClickListener(this);
        mRadarView = (RadarScanView) mViewRoot.findViewById(R.id.radar);
    }

    private void initData(Bundle savedInstanceState) {
        int i = getArguments().getInt(ARG_PLANET_NUMBER);
        String planet = getResources().getStringArray(R.array.planets_array)[i];
        int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                "drawable", getActivity().getPackageName());
        mIvImage.setImageResource(imageId);
    }

    private void showRadar() {
        mRadarView.setVisibility(View.VISIBLE);
        mIvImage.setVisibility(View.GONE);
    }

    private void showNearyUser() {
        mRadarView.setVisibility(View.GONE);
        mIvImage.setVisibility(View.VISIBLE);
    }
}