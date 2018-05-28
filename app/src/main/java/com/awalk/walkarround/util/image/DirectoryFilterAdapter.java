package com.awalk.walkarround.util.image;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.awalk.walkarround.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 2015/12/12.
 */
public class DirectoryFilterAdapter extends BaseAdapter {
    private Context context;
    private String[] from;
    private int[] to;
    List<Map<String, String>> dataList;

    /**
     *
     */
    public DirectoryFilterAdapter(Context con, List<Map<String, String>> data, String[] from, int[] to) {
        this.context = con;
        this.from = from;
        this.to = to;
        this.dataList = data;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return dataList.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.image_chooser_item, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Map<String, String> tempItem = (Map<String, String>) getItem(position);
        holder.firstImage = (ImageView) convertView.findViewById(to[0]);

        ImageLoaderManager.displayImage(context, tempItem.get(from[0]).toString(),R.drawable.default_image, holder.firstImage);
        //ImageManager.from(context).displayImage(holder.firstImage, tempItem.get(from[0]).toString(),
        //        R.drawable.mail_picture_attachfile_icon);
        holder.nameTextView = (TextView) convertView.findViewById(to[1]);
        holder.nameTextView.setText(tempItem.get(from[1]).toString());
        holder.countTextView = (TextView) convertView.findViewById(to[2]);
        holder.countTextView.setText(tempItem.get(from[2].toString()));
        return convertView;
    }

    public static class ViewHolder {
        public ImageView firstImage;
        public TextView nameTextView;
        public TextView countTextView;
    }

}
