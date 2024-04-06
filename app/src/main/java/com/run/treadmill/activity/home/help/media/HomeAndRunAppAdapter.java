package com.run.treadmill.activity.home.help.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;

public class HomeAndRunAppAdapter extends RecyclerView.Adapter<HomeAndRunAppAdapter.ViewHolder> {
    private int[] imgs;
    private String[] apkNames;
    private OnItemClick mOnItemClick;
    private Context context;

    public HomeAndRunAppAdapter(Context context, int[] list) {
        this.context = context;
        if (list == null) {
            imgs = new int[]{};
        } else {
            imgs = list;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_media_app, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.img_media_app.setImageResource(imgs[position]);
        holder.img_media_app.setEnabled(true);
        holder.img_media_app.setSelected(false);

        holder.tv_name.setText(apkNames[position]);
        if (mOnItemClick != null) {
            holder.img_media_app.setOnClickListener(v -> {
                holder.img_media_app.setSelected(true);
                holder.img_media_app.setEnabled(false);
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

    public void setNames(String[] apkNames) {
        this.apkNames = apkNames;
    }

    public interface OnItemClick {
        void setOnItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img_media_app;
        TextView tv_name;

        public ViewHolder(View itemView) {
            super(itemView);
            img_media_app = itemView.findViewById(R.id.img_media_app);
            tv_name = itemView.findViewById(R.id.tv_app_name);
        }
    }

    public void refresh() {
        notifyDataSetChanged();
    }
}