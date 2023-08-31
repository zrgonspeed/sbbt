package com.run.treadmill.thirdapp.main;

import com.run.treadmill.R;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.thirdapp.bean.ThirdApp;
import com.run.treadmill.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ThirdUpdateUtils {
    private static List<ThirdApp> list;

    public static boolean changeLanguage = false;

    // 切换语言后并不会清除list
    private static synchronized void initList() {
        String language = SpManager.getLanguage();
        Logger.d("sp language == " + language);

        if (list == null || changeLanguage) {
            if (language.endsWith("zh")) {
                Logger.d("初始化中文状态");
                initListChina();
            } else {
                Logger.d("初始化非中文状态");
                initListEnglish();
            }
        }

        changeLanguage = false;
    }

    private static synchronized void initListEnglish() {
        ThirdApp youtube = new ThirdApp.Builder("YouTube", "com.google.android.youtube")
                .updateDrawable(R.drawable.btn_media_youtube_1)
                .build();

        ThirdApp chrome = new ThirdApp.Builder("GoogleChrome", "com.android.chrome")
                .updateDrawable(R.drawable.btn_media_chrome_1)
                .build();
        ThirdApp twitter = new ThirdApp.Builder("Twitter", "com.twitter.android")
                .updateDrawable(R.drawable.btn_media_twitter_1)
                .build();
        ThirdApp facebook = new ThirdApp.Builder("Facebook", "com.facebook.katana")
                .updateDrawable(R.drawable.btn_media_facebook_1)
                .build();

        ThirdApp instagram = new ThirdApp.Builder("Instagram", "com.instagram.android")
                .updateDrawable(R.drawable.btn_media_instagram_1)
                .build();

        ThirdApp spotify = new ThirdApp.Builder("Spotify", "com.spotify.music")
                .updateDrawable(R.drawable.btn_media_spotify_1)
                .build();

        ThirdApp netflix = new ThirdApp.Builder("NETFLIX", "com.netflix.mediaclient")
                .updateDrawable(R.drawable.btn_media_netflix_1)
                .build();

        ThirdApp ponymusic = new ThirdApp.Builder("ponymusic", "me.wcy.music")
                .updateDrawable(R.drawable.btn_media_mp3_1)
                .build();

        ThirdApp anplus_A133Bluetooth = new ThirdApp.Builder("Anplus_A133Bluetooth", "com.anplus.bluetooth")
                .updateDrawable(R.drawable.btn_app_bluetooth_1)
                .build();

        ThirdApp anplusMirroring = new ThirdApp.Builder("AnplusMirroring", "com.anplus.tft")
                .updateDrawable(R.drawable.btn_media_screen_mirroring_1)
                .build();

        ThirdApp gms = new ThirdApp.Builder("GooglePlayServices", "com.google.android.gms")
                .updateDrawable(R.drawable.btn_media_google_serive_1)
                .build();

        ThirdApp kinomap = new ThirdApp.Builder("Kinomap", "com.kinomap.training")
                .updateDrawable(R.drawable.btn_media_kinomap_1)
                .build();

        ThirdApp disney = new ThirdApp.Builder("Disneyplus", "com.disney.disneyplus")
                .updateDrawable(R.drawable.btn_media_disney_1)
                .build();

        ThirdApp vod = new ThirdApp.Builder("Vod12", "com.keshet.mako.VOD.intl")
                .updateDrawable(R.drawable.btn_media_12_1)
                .build();

        list = new ArrayList<>();
        list.add(youtube);
        list.add(chrome);
        list.add(twitter);
        list.add(facebook);
        list.add(instagram);
        list.add(spotify);
        list.add(netflix);
        list.add(ponymusic);
        list.add(anplus_A133Bluetooth);

        list.add(anplusMirroring);
        list.add(kinomap);

        list.add(disney);
        list.add(vod);

        list.add(gms);
    }

    private static synchronized void initListChina() {
        ThirdApp fireFox = new ThirdApp.Builder("FireFox", "org.mozilla.firefox")
                .updateDrawable(R.drawable.btn_media_firefox_1)
                .build();
        ThirdApp weibo = new ThirdApp.Builder("weibo", "com.sina.weibo")
                .updateDrawable(R.drawable.btn_media_weibo_1)
                .build();
        ThirdApp iqiyi = new ThirdApp.Builder("iqiyi", "com.qiyi.video.pad")
                .updateDrawable(R.drawable.btn_media_i71_1)
                .build();
        ThirdApp ponymusic = new ThirdApp.Builder("ponymusic", "me.wcy.music")
                .updateDrawable(R.drawable.btn_media_mp3_1)
                .build();
        ThirdApp qqmusicpad = new ThirdApp.Builder("qqmusicpad", "com.tencent.qqmusicpad")
                .updateDrawable(R.drawable.btn_media_qq_music_1)
                .build();
        ThirdApp lebo = new ThirdApp.Builder("HappyCast", "com.hpplay.happyplay.aw")
                .updateDrawable(R.drawable.btn_media_happycast_1)
                .build();
        ThirdApp anplus_A133Bluetooth = new ThirdApp.Builder("Anplus_A133Bluetooth", "com.anplus.bluetooth")
                .updateDrawable(R.drawable.btn_app_bluetooth_1)
                .build();
        ThirdApp gms = new ThirdApp.Builder("GooglePlayServices", "com.google.android.gms")
                .updateDrawable(R.drawable.btn_media_google_serive_1)
                .build();

        list = new ArrayList<>();
        list.add(fireFox);
        list.add(weibo);
        list.add(iqiyi);
        list.add(ponymusic);
        list.add(qqmusicpad);
        list.add(lebo);
        list.add(anplus_A133Bluetooth);
        list.add(gms);
    }

    public static List<ThirdApp> getAppList() {
        initList();
        return list;
    }

    public static String[] getNames() {
        initList();

        String[] arr = new String[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i).name;
        }
        return arr;
    }

    public static String[] getPkgNames() {
        initList();

        String[] arr = new String[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i).packageName;
        }
        return arr;
    }

    public static int[] getUpdateDrawables() {
        initList();

        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i).update_drawable;
        }
        return arr;
    }
}
