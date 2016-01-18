package com.example.jh.comeutils;


import android.os.Handler;

/**
 * Created by jh on 2016/1/12.
 */
public class AsynNetUtils {
    public  interface  Callback{
        void onResponse(String response);
    }
    public static void get (final String url,final Callback callback){
        final Handler mHandle=new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response=NetUtils.get(url);
                mHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        });
    }
    public static void post(final String url,final String content,final Callback callback){
        final Handler mHandler=new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response=NetUtils.post(url, content);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });

            }
        });
    }
}
