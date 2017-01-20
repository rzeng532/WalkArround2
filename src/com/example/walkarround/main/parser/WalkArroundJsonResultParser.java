package com.example.walkarround.main.parser;

import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.model.FriendInfo;
import com.example.walkarround.util.http.HttpUtil;

import java.util.List;

/**
 * Created by Richard on 16/1/10.
 */
public class WalkArroundJsonResultParser {

    public static String parseReturnCode(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        try {
            JSONObject jsonObject = JSONObject.parseObject(str);
            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {
                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);
                if (jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE)) {
                    return jsonResultObject.getString(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String parseRequireCode(String source, String reqCode) {
        if (TextUtils.isEmpty(source)) {
            return "";
        }

        try {
            JSONObject jsonObject = JSONObject.parseObject(source);
            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {

                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);
                if (jsonResultObject != null && jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA)) {

                    JSONObject subResultObject = jsonResultObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA);
                    if (subResultObject != null && subResultObject.containsKey(reqCode)) {
                        return subResultObject.getString(reqCode);
                    }
                    //String data = jsonResultObject.getJSONArray(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA).toJSONString();
                    //return JSONObject.parseArray(data, ContactInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int parseRequireIntCode(String source, String reqCode) {
        if (TextUtils.isEmpty(source)) {
            return -1;
        }

        try {
            JSONObject jsonObject = JSONObject.parseObject(source);
            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {

                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);
                if (jsonResultObject != null && jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA)) {

                    JSONObject subResultObject = jsonResultObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA);
                    if (subResultObject != null && subResultObject.containsKey(reqCode)) {
                        return subResultObject.getInteger(reqCode);
                    }
                    //String data = jsonResultObject.getJSONArray(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA).toJSONString();
                    //return JSONObject.parseArray(data, ContactInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<ContactInfo> parse2NearlyUserModelList(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(str);

            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {

                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);

                if (jsonResultObject != null && jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA)) {
                    String data = jsonResultObject.getJSONArray(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA).toJSONString();
                    return JSONObject.parseArray(data, ContactInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<FriendInfo> parse2FriendList(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        try {
            JSONObject jsonObject = JSONObject.parseObject(str);

            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {

                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);

                if (jsonResultObject != null && jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA)) {
                    String data = jsonResultObject.getJSONArray(HttpUtil.HTTP_RESPONSE_KEY_RESULT_DATA).toJSONString();
                    return JSONObject.parseArray(data, FriendInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static GeoData parseUserCoordinate(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        GeoData coordinateData = null;

        try {
            JSONObject jsonObject = JSONObject.parseObject(str);

            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {

                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);

                if (jsonResultObject != null && jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {
                    JSONObject subJsonResultObject = jsonResultObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);
                    if (subJsonResultObject != null
                            && subJsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_LATITUDE)
                            && subJsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_LONGITUDE)) {
                        double latitude = Double.parseDouble(subJsonResultObject.getString(HttpUtil.HTTP_RESPONSE_KEY_LATITUDE));
                        double longtitude = Double.parseDouble(subJsonResultObject.getString(HttpUtil.HTTP_RESPONSE_KEY_LONGITUDE));
                        coordinateData = new GeoData(latitude, longtitude, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return coordinateData;
    }

    //The example looks like (we need: "objectId": "58807031128fe1006c208941"):
    /*
        {
          "result": {
            "code": 200,
            "result": {
                  "datingStatus": 1,
                  "userId": {
                    "__type": "Pointer",
                    "className": "_User",
                    "objectId": "578def035bbb50005b87b9f2"
                    },
                  "onlineStatus": true,
                  "location": {
                    "__type": "GeoPoint",
                    "latitude": 30.269122,
                    "longitude": 120.111551
                    },
                  "objectId": "58807031128fe1006c208941",
                  "createdAt": "2017-01-19T07:52:17.421Z",
                  "updatedAt": "2017-01-20T08:19:55.324Z"
            }
          }
        }
     */
    public static String parseUserDynamicRecordId(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        String recordId = null;

        try {
            JSONObject jsonObject = JSONObject.parseObject(str);

            if (jsonObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {

                JSONObject jsonResultObject = jsonObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);

                if (jsonResultObject != null && jsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT)) {
                    JSONObject subJsonResultObject = jsonResultObject.getJSONObject(HttpUtil.HTTP_RESPONSE_KEY_RESULT_RESULT);
                    if (subJsonResultObject != null
                            && subJsonResultObject.containsKey(HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID)) {
                        recordId = subJsonResultObject.getString(HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recordId;
    }
}
