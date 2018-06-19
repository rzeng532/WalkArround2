package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;

/**
 * 注册信息
 * Date: 2018-06-14
 *
 * @author cmcc
 */
public class RegisterInfo implements Serializable {

    private UserInfo result;
    private UserInfo results;

    public UserInfo getResult() {
        return result == null ? results : result;
    }

    public void setResult(UserInfo result) {
        this.result = result;
    }

    public UserInfo getResults() {
        return results;
    }

    public void setResults(UserInfo results) {
        this.results = results;
    }

    public class UserInfo implements Serializable {

        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

}
