package com.awalk.walkarround.retrofit.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * if the list has more and refresh ,can extends this, or do not extends it
 * Created by bingbing on 2016/7/9.
 */
@SuppressLint("ParcelCreator")
public class BaseModel implements Parcelable {
    private int count;
    private String previous;
    private String next;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.count);
        dest.writeString(this.previous);
        dest.writeString(this.next);
    }

    public BaseModel() {
    }

    protected BaseModel(Parcel in) {
        this.count = in.readInt();
        this.previous = in.readString();
        this.next = in.readString();
    }

    public String getNext() {
        return next;
    }

    public int getCount() {
        return count;
    }

    public String getPrevious() {
        return previous;
    }
}
