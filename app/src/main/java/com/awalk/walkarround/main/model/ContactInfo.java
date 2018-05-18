package com.awalk.walkarround.main.model;

import com.awalk.walkarround.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard on 16/1/10.
 */
public class ContactInfo implements Serializable {


    /**
     * isContributor : false
     * portrait : {"__type":"File","id":"567d642500b0cff56c29cd79","name":"18867102890","url":"http://ac-nddk6udk.clouddn.com/8Upn3VarPAkm5JMWuJFIhoFQjv5L7q3Ry7PVtN92"}
     * signature : 哈哈哈
     * username : 曾瑞锦
     * birthday : 1985/1/27
     * emailVerified : false
     * mobilePhoneNumber : 18867102890
     * gender : 0
     * authData : null
     * mobilePhoneVerified : true
     * distance : [0.001920000105051201]
     * objectId : 565eb4fd60b25b0435209c10
     * createdAt : 2015-12-02T09:08:13.207Z
     * updatedAt : 2016-01-05T04:26:56.203Z
     */

    private boolean isContributor;
    /**
     * __type : File
     * id : 567d642500b0cff56c29cd79
     * name : 18867102890
     * url : http://ac-nddk6udk.clouddn.com/8Upn3VarPAkm5JMWuJFIhoFQjv5L7q3Ry7PVtN92
     */
    private static final long serialVersionUID = -7248381733189729383L;

    private PortraitEntity portrait;
    private String signature;
    private String username;
    private String birthday;
    private boolean emailVerified;
    private String mobilePhoneNumber;
    private String gender;
    private Object authData;
    private boolean mobilePhoneVerified;
    private String objectId;
    private String createdAt;
    private String updatedAt;
    private List<Double> distance;

    public void setIsContributor(boolean isContributor) {
        this.isContributor = isContributor;
    }

    public void setPortrait(PortraitEntity portrait) {
        this.portrait = portrait;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAuthData(Object authData) {
        this.authData = authData;
    }

    public void setMobilePhoneVerified(boolean mobilePhoneVerified) {
        this.mobilePhoneVerified = mobilePhoneVerified;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDistance(List<Double> distance) {
        this.distance = distance;
    }

    public boolean isIsContributor() {
        return isContributor;
    }

    public PortraitEntity getPortrait() {
        if(portrait == null) {
            portrait = new PortraitEntity();
        }
        return portrait;
    }

    public String getSignature() {
        return signature;
    }

    public String getUsername() {
        return username;
    }

    public String getBirthday() {
        return birthday;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public Object getAuthData() {
        return authData;
    }

    public boolean isMobilePhoneVerified() {
        return mobilePhoneVerified;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<Double> getDistance() {
        if(distance == null) {
            distance = new ArrayList<Double>();
        }
        return distance;
    }

    public class PortraitEntity {
        //@com.google.gson.annotations.SerializedName("__type")
        private String type;
        private int id = R.drawable.default_profile_portrait;
        private String name;
        private String url;

        public void setType(String type) {
            this.type = type;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
