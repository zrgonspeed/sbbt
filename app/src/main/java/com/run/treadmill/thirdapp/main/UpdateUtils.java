package com.run.treadmill.thirdapp.main;

import com.run.treadmill.R;
import com.run.treadmill.thirdapp.bean.ThirdApp;

import java.util.ArrayList;
import java.util.List;

public class UpdateUtils {
    private static List<ThirdApp> list;

    private static synchronized void initList() {
        if (list == null) {
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

            ThirdApp kinomap = new ThirdApp.Builder("Kinomap", "com.kinomap.training")
                    .updateDrawable(R.drawable.btn_media_kinomap_1)
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
            list.add(kinomap);
        }
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
