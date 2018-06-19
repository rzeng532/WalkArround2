package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;
import java.util.List;

import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.main.model.FriendInfo;

/**
 * 联系人信息
 * Date: 2018-06-14
 *
 * @author cmcc
 */
public class ContactsList implements Serializable {

    private List<ContactInfo> results;

    public List<ContactInfo> getResults() {
        return results;
    }

    public void setResults(List<ContactInfo> results) {
        this.results = results;
    }
}
