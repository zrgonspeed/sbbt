package com.run.treadmill.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/20
 */
public class SummaryPagerAdapter extends PagerAdapter {
    private List<View> mViews;

    public SummaryPagerAdapter(List<View> views) {
        if (views == null) {
            this.mViews = new ArrayList<>();
        }
        this.mViews = views;
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViews.get(position));
    }
}