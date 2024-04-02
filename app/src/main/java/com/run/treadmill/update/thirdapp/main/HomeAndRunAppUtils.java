package com.run.treadmill.update.thirdapp.main;

import com.run.treadmill.R;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.thirdapp.bean.ThirdApp;
import com.run.treadmill.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class HomeAndRunAppUtils {
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
                .homeDrawable(R.drawable.btn_home_youtube)
                .runDrawable(R.drawable.btn_home_youtube)
                .viewName("Youtube")
                .build();

        ThirdApp chrome = new ThirdApp.Builder("GoogleChrome", "com.android.chrome")
                .homeDrawable(R.drawable.btn_home_chrome)
                .runDrawable(R.drawable.btn_media_chrome)
                .build();
        ThirdApp twitter = new ThirdApp.Builder("Twitter", "com.twitter.android")
                .homeDrawable(R.drawable.btn_home_twitter)
                .runDrawable(R.drawable.btn_home_twitter)
                .viewName("X")
                .build();
        ThirdApp facebook = new ThirdApp.Builder("Facebook", "com.facebook.katana")
                .homeDrawable(R.drawable.btn_home_facebook)
                .runDrawable(R.drawable.btn_home_facebook)
                .build();

        ThirdApp instagram = new ThirdApp.Builder("Instagram", "com.instagram.android")
                .homeDrawable(R.drawable.btn_home_instagram)
                .runDrawable(R.drawable.btn_home_instagram)
                .build();

        ThirdApp spotify = new ThirdApp.Builder("Spotify", "com.spotify.music")
                .homeDrawable(R.drawable.btn_home_spotify)
                .runDrawable(R.drawable.btn_home_spotify)
                .build();

        ThirdApp netflix = new ThirdApp.Builder("NETFLIX", "com.netflix.mediaclient")
                .homeDrawable(R.drawable.btn_home_netflix)
                .runDrawable(R.drawable.btn_home_netflix)
                .viewName("Netflix")

                .build();

        ThirdApp ponymusic = new ThirdApp.Builder("ponymusic", "me.wcy.music")
                .homeDrawable(R.drawable.btn_home_mp3)
                .runDrawable(R.drawable.btn_home_mp3)
                .viewName("MP3")
                .build();

        ThirdApp mp4 = new ThirdApp.Builder("mp4", "com.softwinner.fireplayer")
                .homeDrawable(R.drawable.btn_home_mp4)
                .runDrawable(R.drawable.btn_home_mp4)
                .viewName("MP4")
                .build();

        ThirdApp kinomap = new ThirdApp.Builder("Kinomap", "com.kinomap.training")
                .homeDrawable(R.drawable.btn_home_kinomap)
                .runDrawable(R.drawable.btn_home_kinomap)
                .viewName("kinomap")
                .build();

        ThirdApp AnplusMirroring = new ThirdApp.Builder("AnplusMirroring", "com.anplus.tft")
                .homeDrawable(R.drawable.btn_home_screen_mirroring)
                .runDrawable(R.drawable.btn_media_screen_mirroring)
                .build();

        ThirdApp disney = new ThirdApp.Builder("Disneyplus", "com.disney.disneyplus")
                .homeDrawable(R.drawable.btn_home_disney)
                .runDrawable(R.drawable.btn_home_disney)
                .viewName("Disney+")

                .build();

        ThirdApp disney2 = new ThirdApp.Builder("Disneyplus2", "in.startv.hotstar.dplus.tv")
                .homeDrawable(R.drawable.btn_home_disney_hotstar)
                .runDrawable(R.drawable.btn_media_disney_hotstar)
                .build();

        ThirdApp vod = new ThirdApp.Builder("Vod12", "com.keshet.mako.VOD.intl")
                .homeDrawable(R.drawable.btn_home_vod)
                .runDrawable(R.drawable.btn_media_vod)
                .build();

        ThirdApp amazonVideo = new ThirdApp.Builder("AmazonVideo", "com.amazon.avod.thirdpartyclient")
                .homeDrawable(R.drawable.btn_home_prime)
                .runDrawable(R.drawable.btn_home_prime)
                .build();

        list = new ArrayList<>();
        list.add(youtube);
        list.add(facebook);
        list.add(netflix);
        list.add(twitter);
        list.add(disney);
        list.add(instagram);
        list.add(amazonVideo);
        list.add(spotify);
        list.add(ponymusic);
        list.add(mp4);
        list.add(kinomap);

        // list.add(chrome);
        // list.add(AnplusMirroring);
    }

    private static synchronized void initListChina() {
        ThirdApp fireFox = new ThirdApp.Builder("FireFox", "org.mozilla.firefox")
                .homeDrawable(R.drawable.btn_home_firefox)
                .runDrawable(R.drawable.btn_media_firefox)
                .build();
        ThirdApp weibo = new ThirdApp.Builder("weibo", "com.sina.weibo")
                .homeDrawable(R.drawable.btn_home_weibo)
                .runDrawable(R.drawable.btn_media_weibo)
                .build();
        ThirdApp iqiyi = new ThirdApp.Builder("iqiyi", "com.qiyi.video.pad")
                .homeDrawable(R.drawable.btn_home_aiqiyi)
                .runDrawable(R.drawable.btn_media_aiqiyi)
                .build();
        ThirdApp ponymusic = new ThirdApp.Builder("ponymusic", "me.wcy.music")
                .homeDrawable(R.drawable.btn_home_mp3)
                .runDrawable(R.drawable.btn_media_mp3)
                .build();
        ThirdApp mp4 = new ThirdApp.Builder("mp4", "com.softwinner.fireplayer")
                .homeDrawable(R.drawable.btn_home_mp4)
                .runDrawable(R.drawable.btn_media_mp4)
                .build();
        ThirdApp qqmusicpad = new ThirdApp.Builder("qqmusicpad", "com.tencent.qqmusicpad")
                .homeDrawable(R.drawable.btn_home_qq_music)
                .runDrawable(R.drawable.btn_media_qq_music)
                .build();
        ThirdApp lebo = new ThirdApp.Builder("lebo", "com.hpplay.happyplay.aw")
                .homeDrawable(R.drawable.btn_home_happycast)
                .runDrawable(R.drawable.btn_media_happycast)
                .build();

        list = new ArrayList<>();
        list.add(fireFox);
        list.add(weibo);
        list.add(iqiyi);
        list.add(ponymusic);
        list.add(mp4);
        list.add(qqmusicpad);
        list.add(lebo);
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

    public static String[] getViewNames() {
        initList();

        String[] arr = new String[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i).viewName;
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

    public static int[] getHomeDrawables() {
        initList();

        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i).home_drawable;
        }
        return arr;
    }

    public static int[] getRunDrawables() {
        initList();

        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i).run_drawable;
        }
        return arr;
    }
}
