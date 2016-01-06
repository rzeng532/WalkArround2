package com.example.walkarround.util.http;

import android.content.Context;


public class HttpTaskBase implements Runnable {
    public interface onResultListener {
        /**
         * 
         * @方法名：onPreTask
         * @描述：(描述这个方法的作用)
         * @输出：void
         * @作者：shijunfeng
         *
         */
        void onPreTask(String requestCode);
        /**
         * 
         * @方法名：onResult
         * @描述：返回任务结果
         * @param object
         * @return
         * @输出：TaskReuslt
         * @作者：shijunfeng
         *
         */
        void onResult(Object object, TaskResult resultCode, String requestCode, String threadId);

        /**
         *
         * @方法名：onResult
         * @描述：返回任务结果
         * @return
         * @输出：
         * @作者：shijunfeng
         *
         */
        void onProgress(int progress, String requestCode);
    }
    public enum TaskResult {
        SUCCEESS,
        FAILED,
        ERROR,
        DELAYED
    }

    public enum TaskType {
        NORMAL,
        DELAY // 可延迟处理
    }
    protected Context mContext;
    private onResultListener mListener;
    private String mId = "";
    private String mRequestCode = "";
    private TaskType mTaskType = TaskType.NORMAL;

    public HttpTaskBase(Context context, onResultListener listener, String requestCode) {
        this.mContext = context;
        this.mListener = listener;
        this.mId = HttpTaskIdGenerater.getId();
        this.mRequestCode = requestCode;
        if (mListener != null)
            mListener.onPreTask(mRequestCode);
    }

    public HttpTaskBase(Context context, onResultListener listener, String requestCode, TaskType taskType) {
        this(context,listener,requestCode);
        this.mTaskType = taskType;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }

    protected Object getId() {
        return this.mId;
    }
    protected Context getContext(){
        return this.mContext;
    }
    protected String getRequestCode(){
        return this.mRequestCode;
    }

    protected TaskType getTaskType(){
        return this.mTaskType;
    }
    
    protected void doResultCallback(Object object, TaskResult resultCode) {
        if (mListener == null) {
            return;
        }
        mListener.onResult(object, resultCode, mRequestCode, mId);
    }

    protected void doProgressCallback(int progress){
        if (mListener == null){
            return;
        }
        mListener.onProgress(progress,mRequestCode);
    }
}
