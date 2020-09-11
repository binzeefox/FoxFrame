package com.binzeefox.foxframe.core.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络业务类 利用的Retrofit
 * @author binze
 * 2019/11/29 15:16
 */
public abstract class RetrofitClient {
    private static final int DEFAULT_CONNECT_TIMEOUT = 10;
    private static final int DEFAULT_READ_TIMEOUT = 20;
    private static final int DEFAULT_WRITE_TIMEOUT = 60;

    private final Retrofit mRetrofit; //retrofit

    /**
     * 私有初始化
     * 子类请配置线程安全的构造器
     */
    protected RetrofitClient() {
        mRetrofit = createRetrofit();
    }


//    *******继承方法↓******

    /**
     * 创建retrofit实例。可子类重写自定义该方法
     * @author binze 2020/6/8 10:10
     */
    protected Retrofit createRetrofit(){
        OkHttpClient okHttp = createOkHttp();
        return new Retrofit.Builder()
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(getBaseUrl())
                .build();
    }

    /**
     * 获取OkHttpClient构造器
     * @author binze 2019/12/10 14:13
     */
    protected OkHttpClient createOkHttp(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.level(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        return requestInterceptor(chain);
                    }
                }).build();
    }

    /**
     * OkHttpClient拦截器
     *
     * 默认添加 Header => Content-Type: application/json;charset=UTF-8
     * @author 狐彻 2020/09/10 14:04
     */
    protected Response requestInterceptor(Interceptor.Chain chain) throws IOException{
        Request request = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .build();
        return chain.proceed(request);
    }

//    *******抽象方法↓******

    /**
     * 获取基本路径
     * @return  项目业务的基本路径
     */
    protected abstract String getBaseUrl();


//    *******公共方法↓******

    /**
     *  获取业务接口Api
     */
    public <T> T getApi(Class<T> apiClass){
        return (T) mRetrofit.create(apiClass);
    }

    /**
     * 获取初始的参数桶
     */
    public static Map<String, String> getRequestParams(){
        return new HashMap<>();
    }

}
