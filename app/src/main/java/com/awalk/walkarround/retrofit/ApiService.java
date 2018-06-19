package com.awalk.walkarround.retrofit;

import com.awalk.walkarround.retrofit.model.CommonHttpResult;
import com.awalk.walkarround.retrofit.model.ContactsList;
import com.awalk.walkarround.retrofit.model.DynamicRecord;
import com.awalk.walkarround.retrofit.model.FriendsList;
import com.awalk.walkarround.retrofit.model.RegisterInfo;
import com.awalk.walkarround.retrofit.model.ResponseInfo;
import com.awalk.walkarround.retrofit.model.UserCoordinate;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 接口
 * Date: 2018-06-10
 *
 * @author cmcc
 */
public interface ApiService {

    /**
     * 创建用户动态记录
     *
     * @param request
     * @return
     */
    @POST("/createUserDynamicData")
    Observable<CommonHttpResult<ResponseInfo>> createUserDynamicData(@Body RequestBody request);

    /**
     * 获取匹配中记录
     *
     * @param request
     * @return
     */
    @POST("/queryUserDynamicData")
    Observable<CommonHttpResult<DynamicRecord>> queryUserDynamicData(@Body RequestBody request);

    /**
     * 更新用户位置和状态信息
     *
     * @param body
     * @return
     */
    @POST("/updateUserDynamicData")
    Observable<CommonHttpResult<DynamicRecord>> updateUserDynamicData(@Body RequestBody body);

    /**
     * 根据用户id查询当前用户走走记录
     *
     * @param body
     * @return
     */
    @POST("/querySpeedDate")
    Observable<CommonHttpResult<DynamicRecord>> querySpeedDate(@Body RequestBody body);

    /**
     * 查询当前用户的好友列表
     *
     * @param body
     * @return
     */
    @POST("/friendList")
    Observable<CommonHttpResult<FriendsList>> queryFriendList(@Body RequestBody body);

    /**
     * 根据当前用户动态信息查询附近好友列表
     *
     * @param body
     * @return
     */
    @POST("/queryNearlyUsers")
    Observable<CommonHttpResult<ContactsList>> queryNearlyUsers(@Body RequestBody body);

    /**
     * 添加好友，如果好友已经存在则修改color值，否则新增Friend记录
     *
     * @param body
     * @return
     */
    @POST("/addFriend")
    Observable<CommonHttpResult<ResponseInfo>> addFriend(@Body RequestBody body);

    /**
     * 喜欢某人，当对方已经喜欢自己，则会修改走走记录状态为2，否则新建一条走走记录
     *
     * @param body
     * @return
     */
    @POST("/likeSomeone")
    Observable<CommonHttpResult<DynamicRecord>> likeSomeone(@Body RequestBody body);

    /**
     * 走起接口，有用户在app中触发走起请求，对用走走状态为3
     *
     * @param body
     * @return
     */
    @POST("/goTogether")
    Observable<CommonHttpResult<ResponseInfo>> goTogether(@Body RequestBody body);

    /**
     * 走走结束接口，app走走到时发起请求，对应走走记录状态为4
     *
     * @param body
     * @return
     */
    @POST("/endSpeedDate")
    Observable<CommonHttpResult<ResponseInfo>> endSpeedDate(@Body RequestBody body);

    /**
     * 取消走走，重置双方状态为1
     *
     * @param body
     * @return
     */
    @POST("/cancelSpeedDate")
    Observable<CommonHttpResult<ResponseInfo>> cancelSpeedDate(@Body RequestBody body);

    /**
     * 好友进入灰色区域
     *
     * @param body
     * @return
     */
    @POST("/friendInActive")
    Observable<CommonHttpResult<ResponseInfo>> friendInActive(@Body RequestBody body);

    /**
     * 设置当前走走color
     *
     * @param body
     * @return
     */
    @POST("/setColor")
    Observable<CommonHttpResult<ResponseInfo>> setColor(@Body RequestBody body);

    /**
     * 走走互评接口，根据走走记录ID去评价
     *
     * @param body
     * @return
     */
    @POST("/evaluationEach")
    Observable<CommonHttpResult<ResponseInfo>> evaluationEach(@Body RequestBody body);

    /**
     * 走走互评接口2，根据用户ID去评价
     *
     * @param body
     * @return
     */
    @POST("/evaluationEach2")
    Observable<CommonHttpResult<ResponseInfo>> evaluationEach2(@Body RequestBody body);

    /**
     * 获取用户坐标，根据用户id查询UserDynamicData中的坐标信息
     *
     * @param body
     * @return
     */
    @POST("/userCoordinate")
    Observable<CommonHttpResult<UserCoordinate>> userCoordinate(@Body RequestBody body);

    /**
     * 注册
     *
     * @param body
     * @return
     */
    @POST("/registe")
    Observable<CommonHttpResult<RegisterInfo>> register(@Body RequestBody body);
}
