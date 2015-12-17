/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.location.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.walkarround.location.activity.LocationActivity;
import com.example.walkarround.location.model.LocationItem;
import com.example.walkarround.R;

import java.util.List;

/**
 * TODO: description
 * Date: 2015-12-16
 *
 * @author Administrator
 */
public class LocationAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<LocationItem> mLocationsList;

    private int mSearchType;

    public LocationAdapter(Context context, List<LocationItem> locationItems, int searchType) {
        this.mInflater = LayoutInflater.from(context);
        this.mLocationsList = locationItems;
        this.mSearchType = searchType;
    }

    @Override
    public int getCount() {
        return mLocationsList == null ? 0 : mLocationsList.size();
    }

    @Override
    public LocationItem getItem(int position) {
        return mLocationsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_location, null, false);
            holder.checkRadioButton = (ImageView) convertView.findViewById(R.id.checked_radioButton);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title_textView);
            holder.subtitleTextView = (TextView) convertView.findViewById(R.id.subtitle_textView);
            if (mSearchType == LocationActivity.KEY_SEARCH) {
                holder.checkRadioButton.setVisibility(View.INVISIBLE);
            }
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        LocationItem location = getItem(position);
        holder.titleTextView.setText(location.getTitle());
        holder.subtitleTextView.setText(location.getSubtitle());
        if (mSearchType == LocationActivity.AROUND_SEARCH) {
            if (location.getChecked()) {
                holder.checkRadioButton.setImageResource(R.drawable.icon_danxuan_pre);
            } else {
                holder.checkRadioButton.setImageResource(R.drawable.icon_danxuan_nm);
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView checkRadioButton;
        TextView titleTextView;
        TextView subtitleTextView;
    }
}
