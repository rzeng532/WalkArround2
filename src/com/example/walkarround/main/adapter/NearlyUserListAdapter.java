package com.example.walkarround.main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
//import com.bumptech.glide.Glide;
import com.example.walkarround.R;
import com.example.walkarround.flingswipe.RotateTextImageView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.image.ImageLoaderManager;

import java.util.List;

/**
 * Created by Shall on 2015-06-23.
 */
public class NearlyUserListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ContactInfo> mUserList;

    public NearlyUserListAdapter(Context mContext, List<ContactInfo> mCardList) {
        this.mContext = mContext;
        this.mUserList = mCardList;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_nearly_user_item, parent, false);
            holder = new ViewHolder();
            holder.mCardImageView = (RotateTextImageView) convertView.findViewById(R.id.helloText);
            holder.mCardName = (TextView) convertView.findViewById(R.id.card_name);
            holder.mCardImageNum = (TextView) convertView.findViewById(R.id.card_image_num);
            holder.mCardYear = (TextView) convertView.findViewById(R.id.card_year);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        Glide.with(mContext)
//                .load(mUserList.get(position).getPortrait().getUrl())
//                .into(holder.mCardImageView);
        //We will user UILImageLoader here rather than Glide. Glide is familiar with Picasso.
        String userPortraitUrl = mUserList.get(position).getPortrait().getUrl();
        if(!TextUtils.isEmpty(userPortraitUrl)) {
            ImageLoaderManager.displayImage(mUserList.get(position).getPortrait().getUrl(), -1, holder.mCardImageView);
        }

        holder.mCardName.setText(mUserList.get(position).getUsername());

        String ageByBirth = ProfileUtil.getAgeByBirth(mUserList.get(position).getBirthday());
        if (TextUtils.isEmpty(ageByBirth)) {
            holder.mCardYear.setText("");
        } else {
            holder.mCardYear.setText(ageByBirth);
        }

        //有数据时显示距离，单位米
        String distance = (mUserList.get(position).getDistance() != null && mUserList.get(position).getDistance().size() > 0)
                ? (int)(mUserList.get(position).getDistance().get(0) * 1000) + mContext.getResources().getString(R.string.common_distance_unit)
                    : "";
        holder.mCardImageNum.setText(distance);

        return convertView;
    }

    class ViewHolder {
        TextView mCardName;
        TextView mCardYear;
        TextView mCardImageNum;
        RotateTextImageView mCardImageView;
    }
}
