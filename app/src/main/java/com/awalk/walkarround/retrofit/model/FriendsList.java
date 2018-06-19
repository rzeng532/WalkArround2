package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;
import java.util.List;

import com.awalk.walkarround.main.model.FriendInfo;

/**
 * 好友信息
 * Date: 2018-06-14
 *
 * @author cmcc
 */
public class FriendsList implements Serializable {

    private List<FriendInfo> results;

    public List<FriendInfo> getResults() {
        return results;
    }

    public void setResults(List<FriendInfo> results) {
        this.results = results;
    }
}
