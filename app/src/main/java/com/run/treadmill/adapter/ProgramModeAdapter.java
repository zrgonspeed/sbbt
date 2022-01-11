package com.run.treadmill.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.run.treadmill.R;

import java.util.Arrays;


public class ProgramModeAdapter extends BaseAdapter {
    private Integer[] drawableList = new Integer[]{};
    private boolean[] drawableSelects;

    private Context context;

    public ProgramModeAdapter(Context c, Integer[] list) {
        drawableList = list;
        drawableSelects = new boolean[list.length];
        Arrays.fill(drawableSelects, false);
        context = c.getApplicationContext();
    }

    @Override
    public int getCount() {
        return drawableList.length;
    }

    @Override
    public Object getItem(int position) {
        return drawableList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_program_mode, null);
        }
        imageView = (ImageView) convertView.getTag();
        if (imageView == null) {
            imageView = (ImageView) convertView.findViewById(R.id.img_item_program_mode);
            convertView.setTag(imageView);
        }
        imageView.setImageResource(drawableList[position]);
        imageView.setSelected(drawableSelects[position]);
        return convertView;
    }

    public boolean[] getDrawableSelects() {
        return drawableSelects;
    }
}
