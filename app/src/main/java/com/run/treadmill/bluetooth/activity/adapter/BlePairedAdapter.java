package com.run.treadmill.bluetooth.activity.adapter;


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
    public List<MyBluetoothDevice> myBluetoothDevices;
    private final List<Short> mRssis;
    private final List<String> mDelBleMac;

    public BlePairedAdapter(Context c) {
        this.mContext = c;
        this.mBleDevices = new ArrayList<>();
        this.myBluetoothDevices = new ArrayList<>();
        this.mRssis = new ArrayList<>();
        this.mDelBleMac = new ArrayList<>();

        BtUtil.setBlePairedDevices(mBleDevices);
        BtUtil.setBlePairedAdapter(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble_paired, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BluetoothDevice mBtDevice = mBleDevices.get(position);
        final MyBluetoothDevice myDevice = myBluetoothDevices.get(position);
//        Logger.d(TAG, "position name = " + mBtDevice.getName());
        if (mBtDevice.getName() == null) {
            return;
        }

//        Logger.i(TAG, "holder == " + holder);
        holder.tv_ble_name.setText(mBtDevice.getName() == null ? "null" : mBtDevice.getName());
        holder.tv_ble_bondstate.setText(mBtDevice.getAddress() == null ? "null" : mBtDevice.getBondState() + "");
        holder.tv_ble_address.setText(mBtDevice.getAddress() == null ? "null" : mBtDevice.getAddress());
        BtUtil.printDevice(TAG, mBtDevice);

        if (mBtDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            boolean isConnect = BtUtil.isConnectClassicBT(mBtDevice.getAddress());

            // boolean isCon2 = BtUtil.isConnecting2(mContext, mBtDevice);
            // boolean isCon = BtUtil.isConnecting(mBtDevice);
//            Logger.e(TAG, "name ==  " + mBtDevice.getName() + "   isCon2 == " + isCon2 + "     isCon == " + isCon);
            if (isConnect) {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_9d2227));
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_link_1);
                holder.btn_ble_connect.setDisconnect();
            } else {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_bluetooth_1);
                holder.btn_ble_connect.setConnect();
            }
            holder.btn_ble_connect.setEnabled(true);
        } else if (mBtDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
            holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_bluetooth_1);
            holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
            holder.btn_ble_connect.setConnecting();
            holder.btn_ble_connect.setEnabled(false);
        } else {
            if (BtUtil.connecting) {
                // 设为连接中
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_bluetooth_1);
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
                holder.btn_ble_connect.setConnecting();
                holder.btn_ble_connect.setEnabled(false);
            } else {
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_bluetooth_1);
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
                holder.btn_ble_connect.setConnect();
                holder.btn_ble_connect.setEnabled(true);
            }
        }

        myDevice.setBTStatus(holder.btn_ble_connect.getStatus());

        holder.btn_ble_connect.setOnClickListener(v -> {
            if (mListener != null) {
                Logger.d(TAG, ">>>>>>>>>>>> onPairedItemClick = " + mBtDevice.getName());
                if (BtUtil.hasConnecting()) {
                    Logger.i(TAG, "其它按钮还有状态");
                    return;
                }

                BtUtil.clickConnBt = true;
                if (BtUtil.connecting) {
                    Logger.e(TAG, "正在连接其他设备");
                    return;
                }
                holder.btn_ble_connect.setEnabled(false);
                if (myDevice.getBtStatus() == 1) {
                    holder.btn_ble_connect.setEnabled(true);
                }
                if (myDevice.getBtStatus() == 3) {
                    // 是当前设备断开
                    BtUtil.disConnectDevice(mContext, mBtDevice);
                    return;
                }

                // holder.btn_ble_connect.setConnecting();
                mListener.onItemClick(mBtDevice);
            }
        });
        holder.btn_ble_delete.setOnClickListener(view -> {
            if (mListener != null) {
                Logger.d(TAG, ">>>>>>>>>>>> onDisconnect = " + mBtDevice.getName());
                mListener.onDisconnect(mBtDevice);
            }
        });

        if (BleDebug.blueDebug) {
            holder.iv_icon.setBackgroundResource(R.color.grassGreen);
            holder.iv_icon.setImageResource(BtUtil.getDeviceType(mBtDevice.getBluetoothClass()));
        }
    }

    @Override
    public boolean hasConnecting() {
        for (MyBluetoothDevice myDevice : myBluetoothDevices) {
            if (myDevice.getBtStatus() == 2) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        if (mBleDevices != null) {
            return mBleDevices.size();
        }
        return 0;
    }

    public void addDevice(BluetoothDevice device, Short rssi) {
        if (!mBleDevices.contains(device) && device.getName() != null) {
            Logger.d(TAG, "addDevice()---- " + device.getName());
            mBleDevices.add(device);
            myBluetoothDevices.add(new MyBluetoothDevice(device));
            mRssis.add(rssi);
            notifyDataSetChanged();
        }
    }

    public void removeDevice(BluetoothDevice device) {
        if (mBleDevices.contains(device)) {
            int id = mBleDevices.lastIndexOf(device);
            mBleDevices.remove(mBleDevices.lastIndexOf(device));
            myBluetoothDevices.remove(id);
            mRssis.remove(id);
            notifyDataSetChanged();
        }
    }

    public void delBle(BluetoothDevice device) {
        mDelBleMac.add(device.getAddress());
        removeDevice(device);
    }

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
            if (BtUtil.isBTEarphone(device)) {
                addDevice(device, (short) 0);
            }
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

            if (BleDebug.blueDebug) {
                tv_ble_bondstate.setVisibility(View.VISIBLE);
                tv_ble_address.setVisibility(View.VISIBLE);
            } else {
                tv_ble_bondstate.setVisibility(View.GONE);
                tv_ble_address.setVisibility(View.GONE);
            }
        }
    }

    private void sortList() {
        for (MyBluetoothDevice myBluetoothDevice : myBluetoothDevices) {
        }
    }
}
