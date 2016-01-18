package http;

import com.google.gson.internal.$Gson$Types;
import com.squareup.okhttp.Request;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by yanglinjie on 2016/1/14.
 */
public abstract class  ResultCallback <T>{
    Type mType;
    public ResultCallback(){
        mType=getSuperclassTypeParameter(getClass());
    }
    static Type getSuperclassTypeParameter(Class<?> subClass){
      Type superClass=subClass.getGenericSuperclass();
        if (superClass instanceof Class){
            throw new RuntimeException("Miss type paramter");
        }
        ParameterizedType parameterizedType=(ParameterizedType)superClass;
        return $Gson$Types.canonicalize(parameterizedType.getActualTypeArguments()[0]);
    }
    public abstract  void onError(Request request,Exception e);
    public abstract  void onResponse(T response);
}
