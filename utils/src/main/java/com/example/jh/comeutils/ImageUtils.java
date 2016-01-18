package com.example.jh.comeutils;

import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Created by yanglinjie on 2016/1/15.
 */
public class ImageUtils {
    /**
     * 根据InputStream获取图片实际的宽度和高度
     *
     * @param imageStream
     * @return
     */
    public static ImageSize getImageSize(InputStream imageStream){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(imageStream,null,options);
        return new ImageSize(options.outWidth,options.outHeight);

    }

    public static int calculateInSampleSize(ImageSize srcSize,ImageSize targetSize){
        int width=srcSize.width;
        int heigth=srcSize.heigth;
        int inSampleSize=1;

        int reqWidth=targetSize.width;
        int reqHeigth=targetSize.heigth;

        if (width>reqWidth&&heigth>reqHeigth){
            int widthRatio=Math.round((float)width/(float)reqWidth);
            int heigthRatio=Math.round((float)heigth/(float)reqHeigth);
            inSampleSize=Math.max(widthRatio,heigthRatio);
        }
        return inSampleSize;
    }

    /**
     * 根据ImageView获适当的压缩的宽和高
     *
     * @param view
     * @return
     */
    public static ImageSize getImageViewSize(View view){
       ImageSize imageSize=new ImageSize();
        imageSize.width=getExpectWidth(view);
        imageSize.heigth=getExpectHeigth(view);
        return imageSize;
    }

    /**
     * 根据view获得期望的高度
     *
     * @param view
     * @return
     */
    private static int getExpectHeigth(View view){
        int heigth=0;
        if (view==null){
         return 0;
        }
        final ViewGroup.LayoutParams params=view.getLayoutParams();
        //如果是WRAP_CONTENT，此时图片还没加载，getWidth根本无效
        if (params!=null&&params.width!=ViewGroup.LayoutParams.WRAP_CONTENT){
            heigth=view.getHeight();
        }
        if (heigth<=0&&params!=null){
            heigth=params.height;
        }
        if (heigth<=0){
            heigth=getImageViewFieldValue(view,"mMaxHeight");//获取设置的最大宽度
        }
        //如果宽度还是没有获取到，憋大招，使用屏幕的宽度
        if (heigth<=0){
            DisplayMetrics displayMetrics=view.getContext().getResources().getDisplayMetrics();
            heigth=displayMetrics.heightPixels;
        }
        return heigth;

    }
    /**
     * 根据view获得期望的宽度
     *
     * @param view
     * @return
     */
    private static int getExpectWidth(View view){
        int width=0;
        if (view==null){
            return 0;
        }
        ViewGroup.LayoutParams params=view.getLayoutParams();
        if (params!=null&&params.width!=ViewGroup.LayoutParams.WRAP_CONTENT){
            width=view.getWidth();
        }
        if (width<=0&&params!=null){
            width=params.width;
        }
        if (width<=0){
            width=getImageViewFieldValue(view,"mMaxWidth");
        }
        if (width<=0){
            DisplayMetrics displayMetrics=view.getContext().getResources().getDisplayMetrics();
            width=displayMetrics.widthPixels;
        }
        return width;
    }

    /**
     * 通过反射获取imageview的某个属性值
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object,String fieldName){
        int value=0;
        try {
             Field field=ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue=field.getInt(object);
            if (fieldValue>0&&fieldValue<Integer.MAX_VALUE){
                value=fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static class ImageSize{
        int width;
        int heigth;
        ImageSize(){

        }
        ImageSize(int width,int heigth){
            this.width=width;
            this.heigth=heigth;
        }

        @Override
        public String toString() {
            return "ImageSize{" +
                    "width=" + width +
                    ", heigth=" + heigth +
                    '}';
        }
    }

}
