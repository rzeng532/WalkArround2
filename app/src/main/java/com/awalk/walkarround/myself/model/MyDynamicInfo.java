package com.awalk.walkarround.myself.model;

import com.awalk.walkarround.Location.model.GeoData;

/**
 * Created by Richard on 2015/12/25.
 */
public class MyDynamicInfo {

    GeoData mCurGeo;
    boolean bOnline = false;
    int mDatingState;

    public int getDatingState() {
        return mDatingState;
    }

    public void setDatingState(int mDatingState) {
        this.mDatingState = mDatingState;
    }

    public MyDynamicInfo(GeoData geo, boolean isOnline, int datingState) {
        this.mCurGeo = geo;
        this.bOnline = isOnline;
        this.mDatingState = datingState;
    }

    public GeoData getCurGeo() {
        return mCurGeo;
    }

    public void setCurGeo(GeoData mCurGeo) {
        this.mCurGeo = mCurGeo;
    }

    public boolean getOnlineState() {
        return bOnline;
    }

    public void setOnlineState(boolean bOnline) {
        this.bOnline = bOnline;
    }

}
