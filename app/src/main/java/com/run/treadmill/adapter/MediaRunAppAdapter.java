package com.run.treadmill.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.run.treadmill.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/09
 */
public class MediaRunAppAdapter extends RecyclerView.Adapter<MediaRunAppAdapter.ViewHolder> {
    private List<Integer> imgs;
    private OnItemClick mOnItemClick;

    public MediaRunAppAdapter(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            imgs = new ArrayList<>();
        } else {
            imgs = list;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_run_media_app, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.img_media_app.setImageResource(imgs.get(position));

        if (mOnItemClick != null) {
            holder.img_media_app.setOnClickListener(v -> {
                mOnItemClick.setOnItemClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return imgs.size();
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