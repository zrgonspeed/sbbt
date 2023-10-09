package com.run.treadmill.util;


import com.google.gson.Gson;
import com.run.treadmill.http.OkHttpCallBack;
import com.run.treadmill.http.OkHttpHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;

public class LocationUtils {

    private static final String TAG = "LocationUtils";

    private static Thread gpsPosThread;

    private static PosCallBack mPosCallBack;

    public static String lat = null;
    public static String lon = null;

    public static void sartGetGpsPos(final long delay) {
        if (gpsPosThread != null) {
            gpsPosThread.interrupt();
            gpsPosThread = null;
        }
        gpsPosThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isGetPos = false;
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(delay);
                        while (!isGetPos) {
                            if (i % 2 == 0) {
                                Ip2Location();
                            } else {
                                Ip2Location2();
                            }
                            Thread.sleep(10000);
                            i++;
                            if ( lat != null && lon != null ) {
                                isGetPos = true;
                            }
                        }
                    } catch (Exception ignore) {
                    }
                    if (isGetPos) {
                        if (mPosCallBack != null) {
                            mPosCallBack.setGpsPos(lat, lon);
                            mPosCallBack = null;
                        }
                        break;
                    }
                }
            }
        });
        gpsPosThread.setName("GpsPos");
        gpsPosThread.start();
    }

    /**
     * http://ip-api.com/json/?lang=en
     * 根据ip获取位置信息
     *
     * @return {"accuracy":50,"as":"AS4538 China Education and Research Network Center",
     * "city":"Nanjing","country":"China","countryCode":"CN","isp":
     * "China Education and Research Network Center","lat":32.0617,"lon":118.7778,"mobile":false,
     * "org":"China Education and Research Network Center","proxy":false,"query":"58.192.32.1",
     * "region":"JS","regionName":"Jiangsu","status":"success","timezone":"Asia/Shanghai","zip":""}
     */
    private static void Ip2Location() {
        String urlStr = "http://ip-api.com/json/?lang=en";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(4000);//读取超时
            urlConnection.setConnectTimeout(4000); // 连接超时
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //找到服务器的情况下,可能还会找到别的网站返回html格式的数据
                InputStream is = urlConnection.getInputStream();

                BufferedReader buff = new BufferedReader(new InputStreamReader(is, "UTF-8"));//注意编码，会出现乱码
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = buff.readLine()) != null) {
                    builder.append(line);
                }
                buff.close();//内部会关闭InputStream
                urlConnection.disconnect();

                String res = builder.toString();

                Logger.d(TAG, "Ip2Location:" + res);
                //if (StringUtils.isJSONString(res)){
                if (isJSONValid3(res)) {
                    JSONObject jsonObject = new JSONObject(res);
                    if (jsonObject.has("lat")) {
                        lat = (String) jsonObject.getString("lat");
                    }
                    if (jsonObject.has("lon")) {
                        lon = (String) jsonObject.getString("lon");
                    }
                }
            }
        } catch (Exception ignore) {
        }
    }

    private static void Ip2Location2() {
        OkHttpHelper.cancel(TAG);
        String urlStr = "http://www.geoplugin.net/json.gp";
        OkHttpHelper.get(urlStr, TAG, new OkHttpCallBack() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onSuccess(Call call, String response) {
                try {
                    if (!isJSONValid3(response)) {
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("geoplugin_latitude")) {
                        lat = (String) jsonObject.getString("geoplugin_latitude");
                    }
                    if (jsonObject.has("geoplugin_longitude")) {
                        lon = (String) jsonObject.getString("geoplugin_longitude");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static boolean isJSONValid3(String jsonInString) {
        try {
            Gson gson = new Gson();
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static void setPosCallBack(PosCallBack callBack) {
        mPosCallBack = callBack;
    }

    public interface PosCallBack {
        void setGpsPos(String latitude, String longitude);
    }

}
