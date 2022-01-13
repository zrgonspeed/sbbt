package com.run.treadmill.manager;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.util.Log;

/*import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;*/
import com.run.android.ShellCmdUtils;
import com.run.treadmill.util.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 统一控制与下控通信的数据
 * @Author GaleLiu
 * @Time 2019/06/10
 */
public class GpsMockManager {
    private final String TAG = "GpsMockManager";
    private static volatile GpsMockManager instance;

    private GpsMockManager() {
    }

    public static GpsMockManager getInstance() {
        if (instance == null) {
            synchronized (GpsMockManager.class) {
                if (instance == null) {
                    instance = new GpsMockManager();
                }
            }
        }
        return instance;
    }

    Context mContext;

    public void init(Context context) {
        mContext = context;
        initGps();
        getLocation();
    }

    private LocationManager locationManager;
    private static final String GPS_LOCATION_NAME = LocationManager.GPS_PROVIDER;
    private boolean isGpsEnabled;
    private String locateType;

    private void initGps() {
        try {
            /*ShellCmdUtils.getInstance().execCommand("settings put secure location_providers_allowed -network");
            ShellCmdUtils.getInstance().execCommand("settings put secure location_providers_allowed -wifi");*/
            ShellCmdUtils.getInstance().execCommand("settings put secure location_providers_allowed +gps");
            grantPermission();
            //获取定位服务
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            //判断是否开启GPS定位功能
            isGpsEnabled = locationManager.isProviderEnabled(GPS_LOCATION_NAME);
            Logger.d("=========isGpsEnabled=====" + isGpsEnabled);
            //定位类型：GPS
            locateType = locationManager.GPS_PROVIDER;
            try {
                locationManager.addTestProvider(
                        locateType
                        , true, true, false, false, true, true, true
                        , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
            } catch (Exception e) {
//                e.printStackTrace();
                Logger.e(e.getMessage());
            }
            try {
                locationManager.setTestProviderEnabled(locateType, true);
            } catch (Exception e) {
//                e.printStackTrace();
                Logger.e(e.getMessage());
            }
            locationManager.setTestProviderStatus(locateType, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

            try {
                //从文件中获取GPS信息
                List<String> data = new ArrayList<String>();
                int i = 2;
                while (i-- > 0) {
                    data.add("46.67757400,7.630668000,634.61");
                }
                String[] coordinates = new String[data.size()];
                data.toArray(coordinates);
                //在子线程中提供数据
                mMockThread = new MockThread(coordinates);
                mMockThread.start();
            } catch (Exception e) {
//                e.printStackTrace();
                Logger.e(e.getMessage());
            }
        } catch (Exception e) {
//                e.printStackTrace();
            Logger.e(e.getMessage());
        }

       /* fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // TODO
                        }
                    }
                });*/

    }

    private void getLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return;
            }
            Logger.d("=========isGpsEnabled==3===" + isGpsEnabled);
            //Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
            // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
            locationManager.requestLocationUpdates(locateType, 0, 0,
                    locationListener);
            //locationManager.addGpsStatusListener(this);
            //locationManager.addNmeaListener((OnNmeaMessageListener) mNmeaListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String GPS_MOCK_PROVIDER = "GpsMockProvider";
    private MockThread mMockThread;
    boolean mbUpdate = true;

    private class MockThread extends Thread {

        private String[] data;

        public MockThread(String[] coordinates) {
            data = coordinates;
        }

        @Override
        public void run() {
            while (mbUpdate) {
                //Latitude,Longitude,altitude
                //String[] parts = "22.54605355,114.02597366,50".split(",");
                //String[] parts = "30,120,50".split(",");
                String[] parts = "40.43,74,50".split(",");
                Double latitude = Double.valueOf(parts[0]);
                Double longitude = Double.valueOf(parts[1]);
                Double altitude = Double.valueOf(parts[2]);
                Location location = new Location(locateType);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAltitude(altitude);
                location.setAccuracy(Criteria.ACCURACY_FINE);
                location.setElapsedRealtimeNanos(System.currentTimeMillis());
                location.setTime(System.currentTimeMillis());
                // show debug message in log
                //Logger.d( "MockThread" + location.toString());

                // 向GpsMockProvider提供一个位置信息
                locationManager.setTestProviderLocation(locateType, location);
                try {
                    Thread.sleep(300);
                    if (Thread.currentThread().isInterrupted())
                        throw new InterruptedException("");
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    //停止模拟位置服务
    public void stopMockLocation() {
        mbUpdate = false;
        try {
            locationManager.clearTestProviderEnabled(locateType);
            locationManager.removeTestProvider(locateType);
            mMockThread.interrupt();
        } catch (Exception e) {
            Log.e("GPS", e.toString());
        }
    }

    LocationListener locationListener = new LocationListener() {
        //这个数据是经过LocationManager
        @Override
        public void onLocationChanged(Location mlocal) {
            if (mlocal == null) return;
            String strResult = "getAccuracy:" + mlocal.getAccuracy() + "\r\n"
                    + "getAltitude:" + mlocal.getAltitude() + "\r\n"
                    + "getBearing:" + mlocal.getBearing() + "\r\n"
                    + "getElapsedRealtimeNanos:" + String.valueOf(mlocal.getElapsedRealtimeNanos()) + "\r\n"
                    + "getLatitude:" + mlocal.getLatitude() + "\r\n"
                    + "getLongitude:" + mlocal.getLongitude() + "\r\n"
                    + "getProvider:" + mlocal.getProvider() + "\r\n"
                    + "getSpeed:" + mlocal.getSpeed() + "\r\n"
                    + "getTime:" + mlocal.getTime() + "\r\n";
            //Log.i("onLocationChanged Show", strResult);
            //Log.i(TAG, "onLocationChanged Show");
            //onStatusChanged("gps", 100, mlocal.getExtras());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }
        /*@Override
        public void onStatusChanged(String provider, int event, Bundle extras) {
            if (event ==100){
                String strResult = extras.getString("test1","") +"\n" +
                        extras.getString("test2","");
                if (mTextView2 != null) {
                    mTextView2.setText(strResult);
                }
            }
        }*/
    };

    //原始数据监听
    GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long arg0, String arg1) {
            byte[] bytes = arg1.getBytes();
        }
    };

    private boolean grantPermission() {
        try {
            /*ShellCmdUtils.getInstance()
                    .execCommand("pm grant com.run.treadmill android.permission.ACCESS_FINE_LOCATION");
            ShellCmdUtils.getInstance()
                    .execCommand("pm grant com.run.treadmill android.permission.ACCESS_COARSE_LOCATION");*/

            /*ShellCmdUtils.getInstance()
                    .execCommand("pm grant com.kinomap.training android.permission.ACCESS_FINE_LOCATION");
            ShellCmdUtils.getInstance()
                    .execCommand("pm grant com.kinomap.training android.permission.ACCESS_COARSE_LOCATION");
            ShellCmdUtils.getInstance()
                    .execCommand("pm grant com.kinomap.training android.permission.BLUETOOTH_ADMIN");
            ShellCmdUtils.getInstance()
                    .execCommand("pm grant com.kinomap.training android.permission.BLUETOOTH_PRIVILEGED");*/

            Object object = mContext.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Method method1 = localClass.getMethod("setMode", int.class, int.class, String.class, int.class);
            method1.setAccessible(true);
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(
                    "com.run.treadmill", 0);
            method1.invoke(object, 58, applicationInfo.uid, "com.run.treadmill", AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //private FusedLocationProviderClient fusedLocationClient;

}