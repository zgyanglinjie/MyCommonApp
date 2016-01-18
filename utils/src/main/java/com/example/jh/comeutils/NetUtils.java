package com.example.jh.comeutils;

import android.accounts.NetworkErrorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * Created by yanglinjie on 2016/1/11.
 */
public class NetUtils {
    //post请求方法
    public static String post(String url,String content){
        HttpURLConnection conn=null;
        try {
            URL mUrl=new URL(url);
            conn= (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);

            String data=content;
            OutputStream  out=conn.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();

            int responseCode=conn.getResponseCode();
            if (responseCode==200){
                InputStream is=conn.getInputStream();
                String response=getStringFromInputStrem(is);
                return  response;
            }else {
                throw  new NetworkErrorException("respons status is"+responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (conn!=null)
                conn.disconnect();
        }
        return null;
    }
    //get网络请求方法
    public static String get(String url){
        HttpURLConnection conn=null;
        try {
            URL mURL=new URL(url);
            conn= (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);

            int responseCode=conn.getResponseCode();
            if (responseCode==200){
                InputStream is=conn.getInputStream();
                String response=getStringFromInputStrem(is);
                return response;
            }else {
                throw  new NetworkErrorException(" response state is"+responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (conn!=null)
                conn.disconnect();
        }
        return null;
    }
    //字符串转换
    private static String getStringFromInputStrem(InputStream is) throws IOException {
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        int len=-1;
        byte []buffer=new byte[1024];
        while((len=is.read(buffer))!=-1){
           os.write(buffer);
        }
        is.close();
        String state=os.toString();
        os.close();
        return state;
    }
}
