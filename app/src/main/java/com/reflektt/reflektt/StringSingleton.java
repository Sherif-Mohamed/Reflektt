package com.reflektt.reflektt;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Shiko on 30/11/2016.
 */

public class StringSingleton {
    private RequestQueue mRequestQueue;
    private static StringSingleton singleton = null;
    private static Context context;
    private StringSingleton() {
        mRequestQueue = Volley.newRequestQueue(context);
    }
    public static StringSingleton getInstance(Context co){
        if (singleton == null) {
            context = co;
            singleton = new StringSingleton();
        }
        return singleton;
    }
    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
