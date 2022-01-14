package com.run.treadmill.bluetooth.adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;
import com.run.treadmill.bluetooth.BleDebug;
import com.run.treadmill.bluetooth.BleSwap.BtUtil;
import com.run.treadmill.bluetooth.window.BleConnectButton;
import com.run.treadmill.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class BlePairedAdapter extends RecyclerView.Adapter<BlePairedAdapter.ViewHolder> implements BleAdapter {
    private final String TAG = this.getClass().getSimpleName();
    public Context mContext;

    public List<BluetoothDevice> mBleDevices;
    private List<Short> mRssis;
    private List<String> mDelBleMac;

    public BlePairedAdapter(Context c) {
        this.mContext = c;
        this.mBleDevices = new ArrayList<BluetoothDevice>();
        this.mRssis = new ArrayList<Short>();
        this.mDelBleMac = new ArrayList<String>();

        BtUtil.setBlePairedDevices(mBleDevices);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble_paired, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BluetoothDevice mDevice = mBleDevices.get(position);
//        Logger.d(TAG, "position name = " + mDevice.getName());
        if (mDevice.getName() == null) {
            return;
        }
        holder.tv_ble_name.setText(mDevice.getName() == null ? "null" : mDevice.getName());
        holder.tv_ble_bondstate.setText(mDevice.getAddress() == null ? "null" : mDevice.getBondState() + "");
        holder.tv_ble_address.setText(mDevice.getAddress() == null ? "null" : mDevice.getAddress());
        BtUtil.printDevice(TAG, mDevice);

        if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            boolean isCon2 = BtUtil.isConnecting2(mContext, mDevice);
            boolean isCon = BtUtil.isConnecting(mDevice);
//            Logger.e(TAG, "name ==  " + mDevice.getName() + "   isCon2 == " + isCon2 + "     isCon == " + isCon);
            if (isCon2) {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_9d2227));
//                holder.tv_ble_name.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.img_connect), null, null, null);
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_link_1);
                holder.btn_ble_connect.setDisconnect();
            } else {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
                holder.tv_ble_name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                holder.btn_ble_connect.setConnect();
            }
            holder.btn_ble_connect.setEnabled(true);
        } else if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
            holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
            holder.tv_ble_name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holder.btn_ble_connect.setConnecting();
            holder.btn_ble_connect.setEnabled(false);
        } else {
            if (!BtUtil.connecting) {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
                holder.tv_ble_name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                holder.btn_ble_connect.setConnect();
                holder.btn_ble_connect.setEnabled(true);
            }
        }

        holder.btn_ble_connect.setOnClickListener(v -> {
            if (mListener != null) {
                Logger.d(TAG, ">>>>>>>>>>>> onPairedItemClick = " + mDevice.getName());
                holder.btn_ble_connect.setEnabled(false);
                if (isHasConnected()) {
                    Logger.d(TAG, ">>>>>>>>>>>> onPairedItemClick = " + mDevice.getName());
                    holder.btn_ble_connect.setEnabled(true);
                }
                holder.btn_ble_connect.setConnecting();
                mListener.onItemClick(mDevice);
            }
        });
        holder.btn_ble_delete.setOnClickListener(view -> {
            if (mListener != null) {
                Logger.d(TAG, ">>>>>>>>>>>> onDisconnect = " + mDevice.getName());
                mListener.onDisconnect(mDevice);
            }
        });

        if (BleDebug.debug) {
            holder.iv_icon.setBackgroundResource(R.color.grassGreen);
            holder.iv_icon.setImageResource(BtUtil.getDeviceType(mDevice.getBluetoothClass()));
        }
    }

    @Override
    public int getItemCount() {
        if (mBleDevices != null) {
            return mBleDevices.size();
        }
        return 0;
    }

    public void addDevice(BluetoothDevice device, Short rssi) {
//        return;
        Logger.i(TAG, "addDevice()---- " + device.getName());
        if (!mBleDevices.contains(device)
//                && !mDelBleMac.contains(device.getAddress())
                && device.getName() != null) {
            mBleDevices.add(device);
            mRssis.add(rssi);
            notifyDataSetChanged();
        }
        Logger.d(TAG, " mBleDevices.size == " + mBleDevices.size());
        Logger.d(TAG, " mBleDevices == " + mBleDevices);
    }

    public void removeDevice(BluetoothDevice device) {
        if (mBleDevices.contains(device)) {
            int id = mBleDevices.lastIndexOf(device);
            mBleDevices.remove(mBleDevices.lastIndexOf(device));
            mRssis.remove(id);
            notifyDataSetChanged();
        }
    }

    public void delBle(BluetoothDevice device) {
        mDelBleMac.add(device.getAddress());
        removeDevice(device);
    }

    public void disConnect(BluetoothDevice device) {
        removeDevice(device);
    }

    public boolean isHasConnected() {
        for (BluetoothDevice device : mBleDevices) {
            if (BtUtil.isConnecting(device)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasConnecting() {
        for (BluetoothDevice device : mBleDevices) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                return true;
            }
        }
        return false;
    }

    /*public BluetoothDevice getConnected() {
        for (BluetoothDevice device : mBleDevices) {
            if ( BtUtil.isConnecting(device) ) {
                return device;
            }
        }
        return null;
    }*/

    public void clearDelBle() {
        mDelBleMac.clear();
    }

    public BluetoothDevice getDevice(int position) {
        if (mBleDevices != null) {
            return mBleDevices.get(position);
        }
        return null;
    }

    public String getAddress(int position) {
        if (mBleDevices != null) {
            return mBleDevices.get(position).getAddress();
        }
        return null;
    }

    public void clearList() {
        mBleDevices.clear();
        mRssis.clear();
        mDelBleMac.clear();
        notifyDataSetChanged();
    }

    private OnBlePairedItemClickListener mListener;

    public void setOnBlePairedItemClickListener(OnBlePairedItemClickListener listener) {
        mListener = listener;
    }

    public void initList(List<BluetoothDevice> list) {
        for (BluetoothDevice device : list) {
            addDevice(device, (short) 0);
        }
    }

    public interface OnBlePairedItemClickListener {
        void onItemClick(BluetoothDevice b);

        void onDisconnect(BluetoothDevice b);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        BleConnectButton btn_ble_connect;
        TextView tv_ble_name;
        TextView tv_ble_address;
        TextView tv_ble_bondstate;
        ImageView iv_icon;
        ImageView btn_ble_delete;

        ViewHolder(View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_ble_name = itemView.findViewById(R.id.tv_ble_name);
            tv_ble_address = itemView.findViewById(R.id.tv_ble_address);
            tv_ble_bondstate = itemView.findViewById(R.id.tv_ble_bondstate);
            btn_ble_connect = itemView.findViewById(R.id.btn_ble_connect);
            btn_ble_delete = itemView.findViewById(R.id.btn_ble_delete);

            if (BleDebug.debug) {
                tv_ble_bondstate.setVisibility(View.VISIBLE);
                tv_ble_address.setVisibility(View.VISIBLE);
            } else {
                tv_ble_bondstate.setVisibility(View.GONE);
                tv_ble_address.setVisibility(View.GONE);
            }
        }
    }
}
