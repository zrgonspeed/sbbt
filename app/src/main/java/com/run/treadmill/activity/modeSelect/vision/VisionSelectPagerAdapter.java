package com.run.treadmill.activity.modeSelect.vision;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class VisionSelectPagerAdapter extends PagerAdapter {

    private ArrayList<View> mViewList;

    public VisionSelectPagerAdapter(@NonNull ArrayList<View> ViewList) {
        mViewList = new ArrayList<>();
        mViewList = ViewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = mViewList.get(position);
        container.addView(v, 0);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}
