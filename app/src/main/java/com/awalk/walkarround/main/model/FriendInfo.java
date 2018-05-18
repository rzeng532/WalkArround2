/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.awalk.walkarround.main.model;

/**
 * TODO: description
 * Date: 2016-11-01
 *
 * @author Administrator
 */
public class FriendInfo {
    /**
     * userId : 578def035bbb50005b87b9f2
     * friendUserId : 578defb81532bc0061f8d9f4
     * color : 2131099716
     * objectId : 5811ab812f301e005c5dc04a
     * createdAt : 2016-10-27T07:23:45.604Z
     * updatedAt : 2016-10-27T07:23:45.604Z
     */

    private String userId;
    private String friendUserId;
    private String color;
    private String objectId;
    private String createdAt;
    private String updatedAt;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public void setColor(String color) {
        this.color = color;
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

    public String getUserId() {
        return userId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public String getColor() {
        return color;
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
}
