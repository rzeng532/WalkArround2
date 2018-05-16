package com.example.walkarround.main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.walkarround.R;
import com.example.walkarround.assistant.AssistantHelper;
import com.example.walkarround.flingswipe.RotateTextImageView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.CommonUtils;
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
        ImageLoaderManager.displayImage(mUserList.get(position).getPortrait().getUrl(),
                mUserList.get(position).getPortrait().getId(), holder.mCardImageView);

        String friendName = mUserList.get(position).getUsername();
        if(friendName.length() > AppConstant.SHORTNAME_LEN) {
            friendName = friendName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
        }
        holder.mCardName.setText(friendName);

        String ageByBirth = ProfileUtil.getAgeByBirth(mUserList.get(position).getBirthday());
        if (TextUtils.isEmpty(ageByBirth)) {
//            holder.mCardYear.setText(mContext.getString(R.string.common_age_secret));
            holder.mCardYear.setText(mContext.getString(R.string.common_age_secret));
        } else {
            holder.mCardYear.setText(ageByBirth);
        }

        //有数据时显示距离
        String distance = null;
        if (AssistantHelper.ASSISTANT_OBJ_ID.equals(mUserList.get(position).getObjectId())) {
            distance = "88" + mContext.getResources().getString(R.string.common_distance_unit_meter);
        } else {
            distance = (mUserList.get(position).getDistance() != null && mUserList.get(position).getDistance().size() > 0)
                    ? CommonUtils.getDistanceStr((int) (mUserList.get(position).getDistance().get(0) * 1000)) : "";
        }
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
