package http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.jh.httpandpicture.R;
import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;

import utils.ImageUtils;

/**
 * Created by yanglinjie on 2016/1/14.
 */
public class OkhttpClientManager {
    private static OkhttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private Gson mGson;
    private OkhttpClientManager(){
        mOkHttpClient=new OkHttpClient();
        mOkHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        mDelivery=new Handler(Looper.getMainLooper());
        mGson=new Gson();
    }
    public static OkhttpClientManager getInstance(){
      if (mInstance==null){
       synchronized (OkhttpClientManager.class){
           if (mInstance==null){
               mInstance=new OkhttpClientManager();
           }
       }
      }
        return mInstance;
    }
    /**
     * 同步的Get请求
     *
     * @param url
     * @return Response
     */
    private Response _getAsyn(String url) throws IOException{
        final Request request=new Request.Builder()
                                .url(url).build();
        Call call=mOkHttpClient.newCall(request);
        Response execute=call.execute();
        return execute;
    }
    /**
     * 同步的Get请求
     *
     * @param url
     * @return 字符串
     */
    private String _getAsString(String url) throws  IOException{
        Response execute=_getAsyn(url);
        return execute.body().string();
    }


    /**
     * 异步的get请求
     *
     * @param url
     * @param callback
     */
    private void _getAsyn(String url,ResultCallback callback){
        final Request request=new Request.Builder()
                .url(url)
                .build();
        deliveryResult(callback,request);

    }
    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return
     */
    private Response _post(String url,Param ...params ) throws IOException{
        Request request=buildPostRequest(url, params);
        Response response=mOkHttpClient.newCall(request).execute();
        return response;
    }

    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return 字符串
     */
    private String _postAsString(String url ,Param...params)throws IOException{
        Response response=_post(url, params);
        return response.body().toString();
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private void _postAsyn(String url,final ResultCallback callback,Param ...params){
        Request request=buildPostRequest(url, params);
        deliveryResult(callback,request);
    }


    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private void _postAsyn(String url,final ResultCallback callback,Map<String,String> params){
        Param []paramsArr=map2Params(params);
        Request request=buildPostRequest(url,paramsArr);
        deliveryResult(callback,request);
    }

    /**
     * 同步基于post的文件上传
     *
     * @param params
     * @return
     */
    private Response _post(String url,File[] files,String[] fileKeys,Param ...params)throws IOException{
        Request request=buildMultipartFormRequest(url, files, fileKeys, params);
        return mOkHttpClient.newCall(request).execute();
    }
    private Response _post(String url,File file,String fileKey)throws IOException{
        Request request=buildMultipartFormRequest(url,new File[]{file},new String[]{fileKey},null);
        return mOkHttpClient.newCall(request).execute();
    }
    private Response _post(String url,File file,String fileKey,Param ... params)throws IOException{
        Request request=buildMultipartFormRequest(url,new File[]{file},new String[]{fileKey},params);
        return mOkHttpClient.newCall(request).execute();
    }
    /**
     * 异步基于post的文件上传
     *
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @throws IOException
     */
    private void _postAsyn(String url,final ResultCallback callback,File[]files,String[]fileKeys,Param...params){
        Request request=buildMultipartFormRequest(url,files,fileKeys,params);
        deliveryResult(callback,request);
    }
    /**
     * 异步基于post的文件上传，单文件不带参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
     private void _postAsyn(String url,final ResultCallback callback,File file,String fileKey){
         Request request=buildMultipartFormRequest(url,new File[]{file},new String[]{fileKey},null);
         deliveryResult(callback,request);
     }
    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
    private void _postAsyn(String url,final ResultCallback callback,File file ,String fileKey,Param...params)throws IOException{
        Request request=buildMultipartFormRequest(url,new File[]{file},new String[]{fileKey},params);
        deliveryResult(callback,request);
    }
    /**
     * 异步下载文件
     *
     * @param url
     * @param destFileDir 本地文件存储的文件夹
     * @param callback
     */
    private void _downloadAsyn(final String url,final String destFileDir,final ResultCallback callback){
        final Request request=new Request.Builder().url(url).build();
        final Call call=mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                sendFailedStringCallback(request, e, callback);
            }

            @Override
            public void onResponse(Response response) {
                InputStream is = null;
                byte[] buf = new byte[1024];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(destFileDir, getFileName(url));
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    sendSuccessResultCallback(file.getAbsolutePath(), callback);
                } catch (IOException e) {
                    sendFailedStringCallback(request, e, callback);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * 加载图片
     *
     * @param view
     * @param url
     * @throws IOException
     */
    private void _displayImage(final ImageView view,final String url,final int errorResId){
        final Request request=new Request.Builder().url(url).build();
        Call call=mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                setErrorResId(errorResId, view);
            }

            @Override
            public void onResponse(Response response) {
                InputStream is=null;
                try {
                    is=response.body().byteStream();
                    ImageUtils.ImageSize actualImageSize=ImageUtils.getImageSize(is);
                    ImageUtils.ImageSize imageViewSize=ImageUtils.getImageViewSize(view);
                    int inSampleSize=ImageUtils.calculateInSampleSize(actualImageSize,imageViewSize);
                    try {
                        is.reset();
                    }catch (IOException e){
                        response=_getAsyn(url);
                        is=response.body().byteStream();
                    }
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inJustDecodeBounds=false;
                    options.inSampleSize=inSampleSize;
                    final Bitmap bm=BitmapFactory.decodeStream(is);
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bm);
                        }
                    });

                } catch (IOException e) {
                    setErrorResId(errorResId,view);
                }finally {
                        try {
                            if (is!=null)
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        });

    }

   private void setErrorResId(final int errorResId,final ImageView view){
       mDelivery.post(new Runnable() {
           @Override
           public void run() {
               mDelivery.post(new Runnable() {
                   @Override
                   public void run() {
                       view.setImageResource(errorResId);
                   }
               });
           }
       });
   }


    private String getFileName(String path){
        int separatorIndex=path.lastIndexOf("/");
        return (separatorIndex<0)?path:path.substring(separatorIndex+1,path.length());
    }
    private Request buildMultipartFormRequest(String url,File[] files,String []fileKeys,Param []params){
        params=validateParam(params);
        MultipartBuilder builder=new MultipartBuilder().type(MultipartBuilder.FORM);
        for (Param param:params){
            builder.addPart(Headers.of("Content-Disposition","form-data; name=\"" + param.key + "\""),
                    RequestBody.create(null,param.value));
        }
        if (files!=null){
            RequestBody fileBody=null;
            for (int i=0;i<files.length;i++){
                File file=files[i];
                String fileName=file.getName();
                fileBody=RequestBody.create(MediaType.parse(guessMimeType(fileName)),file);
                //TODO 根据文件名设置contentType
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),fileBody);
            }
        }
        RequestBody requestBody=builder.build();
        Request request=new Request.Builder().
                url(url).post(requestBody)
                .build();
        return request;
    }
    private String guessMimeType(String path){
        FileNameMap fileNameMap= URLConnection.getFileNameMap();
        String contentTypeFor=fileNameMap.getContentTypeFor(path);
        if(contentTypeFor==null){
             contentTypeFor="application/octet-stream";
        }
        return  contentTypeFor;
    }
    private Param[]  validateParam(Param[] params){
        if (params==null)
            return new Param[0];
        else
            return params;
    }
    private Param[] map2Params(Map<String,String> params){
        if (params==null)return new Param[0];
        int size=params.size();
        Param[] res=new Param[size];
        int i=0;
        for (Map.Entry<String,String> entry:params.entrySet()){
            res[i++]=new Param(entry.getKey(),entry.getValue());
        }
        return res;

    }

    private Request buildPostRequest(String url,Param []params){
        if (params==null)
            params=new Param[0];

        FormEncodingBuilder builder=new FormEncodingBuilder();
        for (Param param:params){
            builder.add(param.key,param.value);
        }
        RequestBody requestBody=builder.build();
        return new Request.Builder().
                url(url).
                post(requestBody).
                build();
    }
    private void deliveryResult(final ResultCallback callback, final Request request){
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
              sendFailedStringCallback(request,e,callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                 final String string=response.body().toString();
                try {
                    if (callback.mType==String.class){
                        sendSuccessResultCallback(string,callback);
                    }else{
                        Object  o=mGson.fromJson(string,callback.mType);
                        sendSuccessResultCallback(o,callback);
                    }
                }catch (com.google.gson.JsonParseException e){ //json
                    sendFailedStringCallback(request,e,callback);
                }
                catch (Exception e){
                    sendFailedStringCallback(request,e,callback);
                }


            }
        });
    }

    private void sendFailedStringCallback(final Request request,final Exception e,final ResultCallback callback){
          mDelivery.post(new Runnable() {
              @Override
              public void run() {
                  if (callback != null)
                      callback.onError(request, e);
              }
          });
    }
    private void sendSuccessResultCallback(final Object obj,final ResultCallback callback){
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback!=null)
                    callback.onResponse(obj);
            }
        });

    }

    //外部使用的方法
    public static Response getAsyn(String url)throws IOException{
      return  OkhttpClientManager.getInstance()._getAsyn(url);
    }
    public static String getAsString(String url)throws IOException{
        return OkhttpClientManager.getInstance()._getAsString(url);
    }
    public static void getAsyn(String url, ResultCallback callBack){
       OkhttpClientManager.getInstance()._getAsyn(url,callBack);
    }
    public  static Response post(String url,Param ...params)throws IOException{
        return  OkhttpClientManager.getInstance()._post(url, params);
    }
    public static String postAsString(String url,Param...params)throws  IOException{
        return OkhttpClientManager.getInstance()._postAsString(url, params);
    }
    public static void postAsyn(String url,final ResultCallback callBack,Param ...params){
        OkhttpClientManager.getInstance()._postAsyn(url,callBack,params);
    }
    public static void postAsyn(String url,final ResultCallback callback,Map<String,String>pams){
        OkhttpClientManager.getInstance()._postAsyn(url, callback, pams);
    }
    public static Response post(String url,File []files,String[]fileKeys,Param ...params)throws IOException{
        return  getInstance()._post(url,files,fileKeys,params);
    }
    public static Response post(String url,File file,String fileKey)throws IOException{
       return getInstance()._post(url,file,fileKey);
    }
    public static Response post(String url,File file ,String fileKey,Param ...params)throws IOException{
        return getInstance()._post(url,file,fileKey,params);
    }
    public static void postAsync(String url,ResultCallback callback,File []files,String[]fileKeys,Param ...params){
        getInstance()._postAsyn(url,callback,files,fileKeys,params);
    }
    public static void postAsync(String url,ResultCallback callback,File file ,String fileKey){
        getInstance()._postAsyn(url,callback,file,fileKey);
    }
    public static void postAsync(String url,ResultCallback callback,File file,String fileKey,Param...params)throws IOException{
        getInstance()._postAsyn(url,callback,file,fileKey,params);
    }
    public static void displayImage(final ImageView imageView,String url,int errorResId){
        getInstance()._displayImage(imageView, url, errorResId);
    }
    public static void displayImage(final ImageView imageView,String url){
        getInstance()._displayImage(imageView,url,-1);
    }
    public static void downloadAsyn(String url,String destDir,ResultCallback callback){
      getInstance()._downloadAsyn(url,destDir,callback);
    }
}
