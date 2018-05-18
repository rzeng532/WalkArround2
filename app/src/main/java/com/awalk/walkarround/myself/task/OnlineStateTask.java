package com.awalk.walkarround.myself.task;

import android.content.Context;
import com.awalk.walkarround.Location.manager.LocationManager;
import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.myself.model.MyDynamicInfo;
import com.awalk.walkarround.util.Logger;

/**
 * Created by Richard on 2015/12/28.
 * It is a background task to update client online state.
 * <p>
 * Client will repeat to update online state on dynamic user data table to keep online state.
 */
public class OnlineStateTask {

    Context mContext;
    private Logger logger = Logger.getLogger(OnlineStateTask.class.getSimpleName());

    Thread mRepeatTask;
    boolean bStopTask = false; //Init state is false;

    private final long ONLINE_STATE_UPDATE_INTERVAL = 30 * 1000; //30 seconds

    enum TaskState {
        INIT,
        RUNNING,
        STOP
    }

    TaskState mCurTaskState = TaskState.INIT;

    private static OnlineStateTask mTask;

    private OnlineStateTask() {
        logger.d("OnlineStateTask constructor.");

        mRepeatTask = new Thread(new Runnable() {
            @Override
            public void run() {
                mCurTaskState = TaskState.RUNNING;
                //Update current account online state every 30 seconds;
                while (true) {
                    logger.d("OnlineStateTask run.");
                    if (bStopTask) {
                        logger.d("OnlineStateTask stop.");
                        return;
                    }

                    GeoData geoData = LocationManager.getInstance(mContext).getCurrentLoc();

                    if (geoData != null) {
                        //Update user dynamic data - online state & GEO.
                        try{
                            ProfileManager.getInstance().updateDynamicData(new MyDynamicInfo(geoData, true, 1), null);
                        } catch (Exception e) {
                            logger.d(" ------ OnlineStateTask exception: ");
                            e.printStackTrace();
                        }
                        logger.d("OnlineStateTask updateDynamicData.");
                    }

                    try {
                        Thread.sleep(ONLINE_STATE_UPDATE_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });

        mCurTaskState = TaskState.INIT;
    }

    public static OnlineStateTask getInstance(Context context) {
        if (mTask == null) {
            synchronized (OnlineStateTask.class) {
                if (mTask == null) {
                    mTask = new OnlineStateTask();
                    mTask.mContext = context;
                }
            }
        }

        return mTask;
    }

    public void startTask() {
        synchronized (OnlineStateTask.class) {
            if (mTask == null || mCurTaskState == TaskState.RUNNING) {
                return;
            }

            logger.d("OnlineStateTask start task.");
            mRepeatTask.start();
        }
    }

    public synchronized void stopTask() {
        synchronized (OnlineStateTask.class) {
            if (mTask == null || mCurTaskState == TaskState.STOP || mCurTaskState == TaskState.INIT) {
                return;
            }

            logger.d("OnlineStateTask stop task.");
            bStopTask = true;

            //Clear task values.
            mCurTaskState = TaskState.STOP;
            mRepeatTask = null;
            mTask = null;
        }
    }
}
