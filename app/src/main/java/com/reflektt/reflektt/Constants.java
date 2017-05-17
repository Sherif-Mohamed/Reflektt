package com.reflektt.reflektt;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Shiko on 19/01/2017.
 */

public class Constants {
    static final String APP_ID = "6CC67C14-0E25-C794-FF4B-07E445FD5100";
    static final String SECRET_KEY = "951F0A83-5AFF-84BD-FF19-F386FFC82C00";
    static final String VERSION = "v1";
    public static final String ACTION_FOLLOW = "com.reflektt.reflektt.FOLLOW";
    public static final String ACTION_UNFOLLOW = "com.reflektt.reflektt.UNFOLLOW";
    public static final String ACTION_UPLOAD = "com.reflektt.reflektt.UPLOAD";
    public static final String PROFILE_PATH = "https://api.backendless.com/"+APP_ID+"/"+VERSION+"/files/profile_pictures/";
    public static final String PHOTO_PATH = "http://reflektt.16mb.com/uploads/";
    public static final String PRODUCTS_PATH = "http://reflektt.16mb.com/Data/";

    public static int calculateNoOfColumns(Context context,int width) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / width);
    }



}
