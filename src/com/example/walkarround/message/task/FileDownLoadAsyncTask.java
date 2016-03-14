package com.example.walkarround.message.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.message.manager.LittleCDbManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageConstant.MessageState;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDownLoadAsyncTask extends AsyncTask<ChatMsgBaseInfo, Integer, ChatMsgBaseInfo> {

    private Logger logger = Logger.getLogger(FileDownLoadAsyncTask.class.getSimpleName());

    private LittleCDbManager messageDbManager;
    private Context mContext;
    private ChatMsgBaseInfo downLoadMsg;
    private boolean isCollectMsg = false;

    public FileDownLoadAsyncTask(Context context, LittleCDbManager messageDbManager,
                                 boolean isCollectMsg) {
        this.messageDbManager = messageDbManager;
        this.isCollectMsg = isCollectMsg;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        File folder = new File(WalkArroundApp.MTC_DATA_PATH + AppConstant.CAMERA_TAKE_PIC_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * 下载
     */
    @Override
    protected ChatMsgBaseInfo doInBackground(ChatMsgBaseInfo... params) {
        ChatMsgBaseInfo message = params[0];
        String urlSite = message.getFileUrlPath();
        if (urlSite == null) {
            return null;
        }
        downLoadMsg = message;
        if (!isCollectMsg) {
            messageDbManager.updateMessageStatus(message.getMsgId(), message.getPacketId(),
                    MessageState.MSG_STATE_RECEIVING);
        }

        String fileName = message.getFilename();
        String localFilePath = WalkArroundApp.MTC_DATA_PATH ;
        if (isCollectMsg) {
            localFilePath += AppConstant.FAVORITE_MSG_FILE_PATH;
        } else {
            switch (message.getMsgType()) {
                case MessageConstant.MessageType.MSG_TYPE_IMAGE:
                    localFilePath += AppConstant.CAMERA_TAKE_PIC_PATH;
                    break;
                case MessageConstant.MessageType.MSG_TYPE_VIDEO:
                    localFilePath += AppConstant.VIDEO_FILE_PATH;
                    break;
                default:
                    localFilePath += AppConstant.MSG_DOWNLOAD_PATH;
                    break;
            }
        }
        File folder = new File(localFilePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        localFilePath += System.currentTimeMillis() + fileName;
        boolean isSuccess = downloadFile(urlSite, localFilePath);
        if (isSuccess) {
            message.setFilePath(localFilePath);
            if (isCollectMsg) {
                //FavoriteMessageManager.updateMessageFileLocalPath(mContext, message.getMsgId(),
                //        localFilePath);
            } else {
                messageDbManager.updateMessageFileLocalPath(message.getMsgId(), message.getPacketId(),
                        localFilePath, MessageState.MSG_STATE_RECEIVED);
            }
        } else {
            //if (!isCollectMsg) {
                messageDbManager.updateMessageStatus(message.getMsgId(), message.getPacketId(),
                        MessageState.MSG_STATE_RECEIVE_FAIL);
            //}
        }
        return isSuccess ? message : null;
    }

    /**
     * 下载文件
     *
     * @param urlSite 文件路径
     * @return 是否下载成功
     */
    private boolean downloadFile(String urlSite, String localFilePath) {
        if (TextUtils.isEmpty(urlSite) || TextUtils.isEmpty(localFilePath)) {
            return false;
        }
        boolean success = false;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlSite);
        HttpResponse response;
        try {
            response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                if (is != null) {
                    File file = new File(localFilePath);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int ch = -1;
                    int count = 0;
                    // 每10k通知UI更新一次
                    final int NOTIFY_SIZE = 1024 * 20;
                    int notifyThreshold = NOTIFY_SIZE;
                    while ((ch = is.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, ch);
                        count += ch;
                        if (count > notifyThreshold) {
                            publishProgress(count);
                            notifyThreshold += NOTIFY_SIZE;
                        }
                    }
                    fileOutputStream.flush();
                    success = true;
                    fileOutputStream.close();
                    is.close();
                }
            }
        } catch (ClientProtocolException e) {
            logger.e("downloadFile ClientProtocolException:" + e.getMessage());
            success = false;
        } catch (IOException e) {
            logger.e("downloadFile IOException:" + e.getMessage());
            success = false;
        }
        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        long downLoadSize = progress[0];
        Intent intent = new Intent();
        intent.setAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE);
        intent.putExtra(MsgBroadcastConstants.BC_VAR_IS_COLLECT_MSG, isCollectMsg);
        intent.putExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, downLoadMsg.getMsgThreadId());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_CONTENT, downLoadMsg.getFilepath());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_DOWN_PRG_ID, downLoadMsg.getMsgId());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_END, downLoadSize);
        intent.putExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_TOTAL, downLoadMsg.getFilesize());
        mContext.sendBroadcast(intent);
    }

    @Override
    protected void onPostExecute(ChatMsgBaseInfo message) {
        if (message != null) {
            Intent intent = new Intent();
            intent.setAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE);
            intent.putExtra(MsgBroadcastConstants.BC_VAR_IS_COLLECT_MSG, isCollectMsg);
            intent.putExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, downLoadMsg.getMsgThreadId());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_CONTENT, downLoadMsg.getFilepath());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_DOWN_PRG_ID, message.getMsgId());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_END, message.getFilesize());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_TOTAL, message.getFilesize());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_FILE_PATH, message.getFilepath());
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_FAIL);
            intent.putExtra(MsgBroadcastConstants.BC_VAR_IS_COLLECT_MSG, isCollectMsg);
            intent.putExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, downLoadMsg.getMsgThreadId());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_DOWN_PRG_ID, downLoadMsg.getMsgId());
            mContext.sendBroadcast(intent);
            Toast.makeText(mContext, R.string.download_fail, Toast.LENGTH_SHORT).show();
        }
    }

}
