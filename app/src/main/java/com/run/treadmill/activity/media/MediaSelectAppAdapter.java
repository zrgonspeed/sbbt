package com.run.treadmill.activity.media;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.run.treadmill.R;

public class MediaSelectAppAdapter extends RecyclerView.Adapter<MediaSelectAppAdapter.ViewHolder> {
    private int[] imgs;
    private OnItemClick mOnItemClick;
    private Context context;

    public MediaSelectAppAdapter(Context context, int[] list) {
        this.context = context;
        if (list == null) {
            imgs = new int[]{};
        } else {
            imgs = list;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_media_app, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.img_media_app.setImageResource(imgs[position]);
        if (mOnItemClick != null) {
            holder.img_media_app.setOnClickListener(v -> {
                mOnItemClick.setOnItemClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return imgs.length;
    }

    public void setOnItemClick(OnItemClick itemClick) {
        this.mOnItemClick = itemClick;
    }

    public interface OnItemClick {
        void setOnItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img_media_app;

        public ViewHolder(View itemView) {
            super(itemView);
            img_media_app = (ImageView) itemView.findViewById(R.id.img_media_app);
        }
    }
}