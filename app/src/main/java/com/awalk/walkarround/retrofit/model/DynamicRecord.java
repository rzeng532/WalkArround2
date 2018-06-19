package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;

/**
 * 动态信息
 * Date: 2018-06-14
 *
 * @author cmcc
 */
public class DynamicRecord implements Serializable {

    private DynamicRecordInfo result;
    private DynamicRecordInfo results;

    public DynamicRecordInfo getResult() {
        return result == null ? results : result;
    }

    public void setResult(DynamicRecordInfo result) {
        this.result = result;
    }

    public DynamicRecordInfo getResults() {
        return results;
    }

    public void setResults(DynamicRecordInfo results) {
        this.results = results;
    }

    public class DynamicRecordInfo implements Serializable {

        private String objectId;
        private String status;
        private String toUser;
        private String fromUser;
        private String color;
        private String datingStatus;

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getToUser() {
            return toUser;
        }

        public void setToUser(String toUser) {
            this.toUser = toUser;
        }

        public String getFromUser() {
            return fromUser;
        }

        public void setFromUser(String fromUser) {
            this.fromUser = fromUser;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getDatingStatus() {
            return datingStatus;
        }

        public void setDatingStatus(String datingStatus) {
            this.datingStatus = datingStatus;
        }
    }

}
