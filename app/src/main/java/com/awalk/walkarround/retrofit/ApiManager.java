package com.awalk.walkarround.retrofit;

import com.alibaba.fastjson.JSONObject;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.retrofit.model.CommonHttpResult;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.FriendsList;
import com.awalk.walkarround.retrofit.model.RegisterInfo;
import com.awalk.walkarround.retrofit.model.ResponseInfo;
import com.awalk.walkarround.retrofit.model.UserCoordinate;
import com.awalk.walkarround.util.http.HttpUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 接口管理
 * Date: 2018-06-10
 *
 * @author mass
 */
public class ApiManager {

    /**
     * 创建用户动态记录
     *
     * @param userId
     * @param apiListener
     */
    public static void createUserDynamicData(String userId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.createUserDynamicData(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("createUserDynamicData") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 更新用户位置信息
     */
    public static void updateDynamicData(String userId, double latitude, double longitude, ApiListener apiListener) {
        updateDynamicData(userId, latitude, longitude, 0, apiListener);
    }

    /**
     * 更新用户位置和状态信息
     *
     * @param datingStatus
     */
    public static void updateDynamicData(String userId, double latitude, double longitude, int datingStatus, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        if (datingStatus > 0) {
            param.put(HttpUtil.HTTP_PARAM_DYN_DATA_DATE_STATE, datingStatus);
        }
        param.put(HttpUtil.HTTP_PARAM_DYN_DATA_LATITUDE, latitude);
        param.put(HttpUtil.HTTP_PARAM_DYN_DATA_LONGITUDE, longitude);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<DynamicRecord>> observable = apiService.updateUserDynamicData(requestBody);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<DynamicRecord>>("updateUserDynamicData") {
                    @Override
                    protected void onSuccess(CommonHttpResult<DynamicRecord> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 查询当前用户动态数据
     *
     * @param userId
     * @param apiListener
     */
    public static void queryUserDynamicData(String userId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<DynamicRecord>> observable = apiService.queryUserDynamicData(requestBody);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<DynamicRecord>>("queryUserDynamicData") {
                    @Override
                    protected void onSuccess(CommonHttpResult<DynamicRecord> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 查询当前用户的好友列表，查询Friend表
     *
     * @param userObjId
     * @param count
     * @param apiListener
     */
    public static void getFriendList(String userObjId, int count, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userObjId);
        param.put(HttpUtil.HTTP_PARAM_FRIEND_LIST_COUNT, count);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<FriendsList>> observable = apiService.queryFriendList(requestBody);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<FriendsList>>("queryFriendList") {
                    @Override
                    protected void onSuccess(CommonHttpResult<FriendsList> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 添加好友，如果好友已经存在则修改color值，否则新增Friend记录
     *
     * @param userObjId
     * @param friendId
     * @param color
     * @param apiListener
     */
    public static void addFriend(String userObjId, String friendId, String color, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userObjId);
        param.put(HttpUtil.HTTP_PARAM_FRIEND_USER_ID, friendId);
        param.put(HttpUtil.HTTP_PARAM_SPEEDDATE_COLOR, color);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.addFriend(requestBody);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("addFriend") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 根据用户id查询当前用户走走记录(一个用户当前有且仅有一条走走记录，状态为2双方喜欢，3走起，4结束)
     *
     * @param userObjId
     * @param apiListener
     */
    public static void querySpeedDate(String userObjId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userObjId);


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<DynamicRecord>> observable = apiService.querySpeedDate(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<DynamicRecord>>("querySpeedDate") {
                    @Override
                    protected void onSuccess(CommonHttpResult<DynamicRecord> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 根据当前用户动态信息查询附近好友列表
     *
     * @param dynamicDataId
     * @param apiListener
     */
    public static void queryNearlyUsers(String dynamicDataId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_QUERY_NEARLY_USERS_ID, dynamicDataId);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<List<ContactInfo>>> observable = apiService.queryNearlyUsers(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<List<ContactInfo>>>("queryNearlyUsers") {
                    @Override
                    protected void onSuccess(CommonHttpResult<List<ContactInfo>> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 喜欢某人，当对方已经喜欢自己，则会修改走走记录状态为2，否则新建一条走走记录
     *
     * @param fromUserId
     * @param toUserId
     */
    public static void likeSomeone(String fromUserId, String toUserId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_LIKE_SOMEONE_FROM, fromUserId);
        param.put(HttpUtil.HTTP_PARAM_LIKE_SOMEONE_TO, toUserId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<DynamicRecord>> observable = apiService.likeSomeone(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<DynamicRecord>>("likeSomeone") {
                    @Override
                    protected void onSuccess(CommonHttpResult<DynamicRecord> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 走起接口，有用户在app中触发走起请求，对用走走状态为3
     *
     * @param speedDateId
     * @param apiListener
     */
    public static void goTogether(String speedDateId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDateId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.goTogether(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("goTogether") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 走走结束接口，app走走到时发起请求，对应走走记录状态为4
     *
     * @param speedDateId
     * @param apiListener
     */
    public static void endSpeedDate(String speedDateId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDateId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.endSpeedDate(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("endSpeedDate") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 取消走走，重置双方状态为1
     *
     * @param speedDateId
     * @param apiListener
     */
    public static void cancelSpeedDate(String speedDateId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDateId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.cancelSpeedDate(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("cancelSpeedDate") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 走走互评接口，根据走走记录ID去评价
     *
     * @param userId
     * @param honesty
     * @param talkative
     * @param temperament
     * @param seductive
     * @param speedDateId
     * @param apiListener
     */
    public static void evaluationEach(String userId, int honesty, int talkative, int temperament,
                                      int seductive, String speedDateId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDateId);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_HONEST, honesty);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TALK_STYLE, talkative);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TEMPERAMENT, temperament);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_SEDUCTIVE, seductive);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.evaluationEach(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("evaluationEach") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 走走互评接口2，根据用户ID去评价
     *
     * @param userId
     * @param honesty
     * @param talkative
     * @param temperament
     * @param seductive
     * @param toUserId
     * @param apiListener
     */
    public static void evaluationEach(String userId, String toUserId, int honesty, int talkative,
                                      int temperament, int seductive, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_LIKE_SOMEONE_TO, toUserId);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_HONEST, honesty);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TALK_STYLE, talkative);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TEMPERAMENT, temperament);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_SEDUCTIVE, seductive);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.evaluationEach2(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("evaluationEach2") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 走走路径报告接口，定时上报用户走走的坐标
     *
     * @param speedDateId
     * @param latitude
     * @param longitude
     * @param apiListener
     */
    public static void datingRoute(String speedDateId, String latitude, String longitude, ApiListener apiListener) {

    }

    /**
     * 好友进入灰色区域
     */
    public static void friendInActive(String userId, String friendUserId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_FRIEND_USER_ID, friendUserId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.friendInActive(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("friendInActive") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 设置当前走走color
     *
     * @param speedDateId
     * @param color
     * @param apiListener
     */
    public static void setColor(String speedDateId, String color, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDateId);
        param.put(HttpUtil.HTTP_PARAM_SPEEDDATE_COLOR, color);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<ResponseInfo>> observable = apiService.setColor(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<ResponseInfo>>("setColor") {
                    @Override
                    protected void onSuccess(CommonHttpResult<ResponseInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 获取用户坐标，根据用户id查询UserDynamicData中的坐标信息
     */
    public static void getUserCoordinate(String userId, final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USR_COORDINATE, userId);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<UserCoordinate>> observable = apiService.userCoordinate(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<UserCoordinate>>("userCoordinate") {
                    @Override
                    protected void onSuccess(CommonHttpResult<UserCoordinate> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });

    }

    /**
     * 更新用户密码
     *
     * @param phone
     * @param password
     * @param apiListener
     */
    public static void updatePassword(String phone, String password, ApiListener apiListener) {

    }

    /**
     * 注册
     *
     * @param username
     * @param phone
     * @param password
     * @param gender
     * @param apiListener
     */
    public static void register(String username, String phone, String password, String gender,
                                final ApiListener apiListener) {
        JSONObject param = new JSONObject();
        try {
            param.put(HttpUtil.HTTP_PARAM_USER_NAME, new String(username.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        param.put(HttpUtil.HTTP_PARAM_PASSWORD, password);
        param.put(HttpUtil.HTTP_PARAM_GENDER, gender);
        param.put(HttpUtil.HTTP_PARAM_PHONE, phone);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                param.toString());
        ApiService apiService = RetrofitManager.getInstance().getServices();

        Observable<CommonHttpResult<RegisterInfo>> observable = apiService.register(requestBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<CommonHttpResult<RegisterInfo>>("register") {
                    @Override
                    protected void onSuccess(CommonHttpResult<RegisterInfo> object, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onSuccess(object.getResult().getCode(), object.getResult().getData());
                    }

                    @Override
                    protected void onFailed(int code, String message) {
                        if (apiListener == null) {
                            return;
                        }
                        apiListener.onFailed(new Exception(code + ", " + message));
                    }

                });
    }

    /**
     * 举报用户接口
     *
     * @param userId
     * @param reportedUserId
     * @param code
     */
    public void report(String userId, String reportedUserId, int code, ApiListener apiListener) {

    }

    /**
     * 获取走走举报类型列表
     */
    public void reportTypeList(ApiListener apiListener) {

    }

    /**
     * 获取走走意见反馈类型列表
     */
    public void feedbackTypeList(ApiListener apiListener) {

    }

    /**
     * 新增反馈
     *
     * @param userId
     * @param type
     * @param mobilePhone
     * @param img
     * @param content
     * @param apiListener
     */
    public void addFeedback(String userId, String type, String mobilePhone,
                            String img, String content, ApiListener apiListener) {

    }

    /**
     * 编辑反馈
     *
     * @param feedbackId
     * @param img
     * @param content
     * @param apiListener
     */
    public void editFeedback(String feedbackId, String img, String content, ApiListener apiListener) {

    }
}
