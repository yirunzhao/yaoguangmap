package com.example.baidumap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class IndoorListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> floors;
    public IndoorListAdapter(Context context,ArrayList<String> floors){
        this.mContext = context;
        mLayoutInflater.from(context);
        this.floors = floors;
    }

    @Override
    public int getCount() {
//        return 0;
        return floors.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    static class ViewHolder{
        public TextView tvFloor;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.layout_indoor_list_item,null);
            holder = new ViewHolder();
            holder.tvFloor = convertView.findViewById(R.id.floor_item);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.tvFloor.setText(floors.get(position));
        return null;
    }
}
