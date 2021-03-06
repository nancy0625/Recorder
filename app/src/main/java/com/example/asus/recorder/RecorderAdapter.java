package com.example.asus.recorder;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.asus.recorder.MainActivity.Recorder;

import java.util.List;

/**
 * Created by asus on 2017/3/4.
 */
public class RecorderAdapter extends ArrayAdapter<Recorder> {


    private int mMinItemWidth;
    private int mMaxItemWidth;

    private LayoutInflater mInflater;

    public RecorderAdapter(Context context, List<Recorder> datas) {
        super(context, -1,datas);

        mInflater = LayoutInflater.from(context);

        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mMaxItemWidth =(int)( outMetrics.widthPixels * 0.7f);
        mMinItemWidth = (int)(outMetrics.widthPixels * 0.2f);

    }
    private class ViewHolder
    {
        TextView seconds;
        View length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_recorder,parent,false);
            holder = new ViewHolder();
            holder.seconds = (TextView)convertView.findViewById(R.id.id_recorder_time);
            holder.length = convertView.findViewById(R.id.id_recorder_length);

            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.seconds.setText(Math.round(getItem(position).time) + "\"");
        ViewGroup.LayoutParams Ip = holder.length.getLayoutParams();
        Ip.width =(int) (mMinItemWidth + (mMaxItemWidth/60f) * getItem(position).time);

        return convertView;
    }

}
