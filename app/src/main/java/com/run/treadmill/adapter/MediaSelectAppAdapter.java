package com.run.treadmill.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img_media_app;

        public ViewHolder(View itemView) {
            super(itemView);
            img_media_app = (ImageView) itemView.findViewById(R.id.img_media_app);
        }
    }
}
