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

public class BleAvaAdapter extends RecyclerView.Adapter<BleAvaAdapter.ViewHolder> implements BleAdapter {
    private final String TAG = this.getClass().getSimpleName();
    public Context mContext;

    public List<BluetoothDevice> mBleDevices;
    public List<MyBluetoothDevice> myBluetoothDevices;

    private List<Short> mRssis;
    private List<String> mDelBleMac;

    public BleAvaAdapter(Context c) {
        this.mContext = c;
        this.mBleDevices = new ArrayList<BluetoothDevice>();
        this.myBluetoothDevices = new ArrayList<MyBluetoothDevice>();
        this.mRssis = new ArrayList<Short>();
        this.mDelBleMac = new ArrayList<String>();

        BtUtil.setBleAvaAdapter(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble_ava, parent, false));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BluetoothDevice mDevice = mBleDevices.get(position);
        final MyBluetoothDevice myDevice = myBluetoothDevices.get(position);

        if (mDevice.getName() == null) {
            return;
        }
        holder.tv_ble_name.setText(mDevice.getName() == null ? "null" : mDevice.getName());
        holder.tv_ble_address.setText(mDevice.getAddress() == null ? "null" : mDevice.getAddress());
        holder.tv_ble_bondstate.setText(mDevice.getAddress() == null ? "null" : mDevice.getBondState() + "");
        BtUtil.printDevice(TAG, mDevice);

        if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            boolean isCon = BtUtil.isConnecting(mDevice);
            if (isCon) {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_9d2227));
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_link_1);
                holder.btn_ble_connect.setDisconnect();
            } else {
                holder.tv_ble_name.setTextColor(ContextCompat.getColor(mContext, R.color.color_2f3031));
                holder.iv_icon.setBackgroundResource(R.drawable.btn_setting_bluetooth_1);
                holder.btn_ble_connect.setConnect();
            }
            holder.btn_ble_connect.setEnabled(true);

        } else if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
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

        myDevice.setBTStatus(holder.btn_ble_connect.getStatus());


        holder.btn_ble_connect.setOnClickListener(v -> {
            if (mListener != null) {
                Logger.d(TAG, ">>>>>>>>>>>> onAvaItemClick = " + mDevice.getName());
//                if (BtUtil.clickConnBt) {
//                    return;
//                }

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
                // 断开已连接的设备
                BtUtil.disConnectCurrentDevice(mContext);
                if (myDevice.getBtStatus() == 3) {
                    // 是当前设备断开
                    return;
                }

                holder.btn_ble_connect.setConnecting();
                mListener.onItemClick(mDevice);
            }
        });

        if (BleDebug.debug) {
            holder.iv_icon.setBackgroundResource(R.color.grassGreen);
            holder.iv_icon.setImageResource(BtUtil.getDeviceType(mDevice.getBluetoothClass()));
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
        Logger.d(TAG, "addDevice() == " + device.getName() + "       rssi == " + rssi);

        if (!mBleDevices.contains(device)
                && !mDelBleMac.contains(device.getAddress())
                && device.getName() != null) {

            // 排除已配对列表中的
            if (BtUtil.getPairedDevices().contains(device)) {
                return;
            }

            Logger.i(TAG, "加进去了addDevice() == " + device.getName() + "       rssi == " + rssi);
            mBleDevices.add(device);
            myBluetoothDevices.add(new MyBluetoothDevice(device));
            mRssis.add(rssi);
            notifyDataSetChanged();
        }
        Logger.d(TAG, " mBleDevices.size == " + mBleDevices.size());
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

    private OnBleAvaItemClickListener mListener;

    public void setOnBleAvaItemClickListener(OnBleAvaItemClickListener listener) {
        mListener = listener;
    }

    public void updateItem(BluetoothDevice device) {
        Logger.i(TAG, "device == " + device);
        notifyItemChanged(mBleDevices.indexOf(device));
    }

    public interface OnBleAvaItemClickListener {

        void onItemClick(BluetoothDevice b);

        void onDisconnect(BluetoothDevice b);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        BleConnectButton btn_ble_connect;
        TextView tv_ble_name;
        TextView tv_ble_bondstate;
        TextView tv_ble_address;
        ImageView iv_icon;

        ViewHolder(View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_ble_name = itemView.findViewById(R.id.tv_ble_name);
            tv_ble_bondstate = itemView.findViewById(R.id.tv_ble_bondstate);
            tv_ble_address = itemView.findViewById(R.id.tv_ble_address);
            btn_ble_connect = itemView.findViewById(R.id.btn_ble_connect);

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
