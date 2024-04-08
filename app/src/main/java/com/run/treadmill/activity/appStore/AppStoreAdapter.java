package com.run.treadmill.activity.appStore;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.run.treadmill.R;

import java.util.ArrayList;
import java.util.List;


public class AppStoreAdapter extends RecyclerView.Adapter<AppStoreAdapter.ViewHolder> {
    private List<AppBean.AppInfo> apps;
    private OnItemClickListener mListener;

    public AppStoreAdapter(List<AppBean.AppInfo> list) {
        apps = new ArrayList<>();
        apps.addAll(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_update, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppBean.AppInfo app = apps.get(position);
        holder.img_app_icon.setImageResource(app.imgId);
        holder.tv_app_name.setText(app.getName());
        switch (app.isUpdate) {
            case AppBean.UPDATE:
                holder.btn_update.setEnabled(true);
                holder.btn_update.setText(R.string.string_app_store_update);
                break;
            case AppBean.NO_UPDATE:
                holder.btn_update.setEnabled(false);
                holder.btn_update.setText(R.string.string_app_store_update);
                break;
            case AppBean.DOWNLOAD_FAIL:
                holder.btn_update.setEnabled(true);
                holder.btn_update.setText(R.string.string_app_store_update_fail);
                break;
            case AppBean.WAIT_DOWNLOAD:
                holder.btn_update.setEnabled(false);
                holder.btn_update.setText(R.string.string_app_store_wait_download);
                break;
            case AppBean.DOWNLOADING:
                holder.btn_update.setEnabled(false);
                holder.btn_update.setText(String.format("%d %%", app.progress));
                break;
            case AppBean.WAIT_INSTALL:
                holder.btn_update.setEnabled(false);
                holder.btn_update.setText(R.string.string_app_store_wait_install);
                break;
            case AppBean.INSTALLING:
                holder.btn_update.setEnabled(false);
                holder.btn_update.setText(R.string.string_app_store_installing);
                break;
            case AppBean.CHECKING:
                holder.btn_update.setEnabled(false);
                holder.btn_update.setText(R.string.string_app_store_check_md5);
                break;
            default:
                break;
        }

        holder.btn_update.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void addItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_app_icon;
        TextView tv_app_name;
        Button btn_update;

        ViewHolder(View itemView) {
            super(itemView);
            img_app_icon = (ImageView) itemView.findViewById(R.id.img_app_icon);
            tv_app_name = (TextView) itemView.findViewById(R.id.tv_app_name);
            btn_update = (Button) itemView.findViewById(R.id.btn_update);
        }
    }
}