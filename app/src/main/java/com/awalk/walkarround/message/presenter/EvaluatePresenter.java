package com.awalk.walkarround.message.presenter;

import android.content.Context;

import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.message.iview.EvaluateView;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.task.AsyncTaskLoadFriendsSession;
import com.awalk.walkarround.message.util.MessageConstant;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.retrofit.ApiListener;
import com.awalk.walkarround.retrofit.ApiManager;
import com.awalk.walkarround.retrofit.model.ResponseInfo;
import com.awalk.walkarround.util.http.HttpUtil;
import com.awalk.walkarround.util.http.ThreadPoolManager;

/**
 * EvaluatePresenter
 * Date: 2018-06-10
 *
 * @author mass
 */
public class EvaluatePresenter extends BasePresenter<EvaluateView> {

    /**
     * 首次评价对方
     *
     * @param userId
     * @param honesty
     * @param talkative
     * @param temperament
     * @param seductive
     * @param speedDateId
     */
    public void evaluateBetweenNoFriends(final Context context, final String userId, int honesty, int talkative, int temperament,
                                         int seductive, String speedDateId,
                                         final long threadId, final String friend) {
        ApiManager.evaluationEach(userId, honesty, talkative, temperament, seductive, speedDateId,
                new ApiListener<ResponseInfo>() {
                    @Override
                    public void onSuccess(String code, ResponseInfo data) {
                        if (HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code)) {
                            int colorIndex = WalkArroundMsgManager.getInstance(context).getConversationColorIndex(threadId);
                            WalkArroundMsgManager.getInstance(context).updateConversationStatus(threadId, MessageUtil.WalkArroundState.STATE_END);
                            addFriend(userId, friend, Integer.toString(colorIndex));
                        }
                        if (mView != null) {
                            mView.evaluateResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (mView != null) {
                            mView.evaluateResult(false, null);
                        }
                    }
                });

    }

    /**
     * 添加朋友
     *
     * @param userId
     * @param friendId
     * @param color
     */
    public void addFriend(String userId, String friendId, String color) {
        ApiManager.addFriend(userId, friendId, color, new ApiListener<ResponseInfo>() {
            @Override
            public void onSuccess(String code, ResponseInfo data) {
                ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_END);
                if (mView != null) {
                    mView.addFriendResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (mView != null) {
                    mView.addFriendResult(false, null);
                }
            }
        });
    }

    /**
     * 多次评价
     *
     * @param userId
     * @param toUserId
     * @param honesty
     * @param talkative
     * @param temperament
     * @param seductive
     */
    public void evaluateBetweenOldFriends(final Context context, String userId, String toUserId, int honesty, int talkative, int temperament,
                                          int seductive, final long threadId) {
        ApiManager.evaluationEach(userId, toUserId, honesty, talkative, temperament, seductive,
                new ApiListener<ResponseInfo>() {
                    @Override
                    public void onSuccess(String code, ResponseInfo data) {
                        if (HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code)) {
                            WalkArroundMsgManager.getInstance(context).updateConversationStatus(threadId, MessageUtil.WalkArroundState.STATE_END_IMPRESSION);
                        }
                        if (mView != null) {
                            mView.evaluateResult(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(code), data);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (mView != null) {
                            mView.evaluateResult(false, null);
                        }
                    }
                });

    }
}
