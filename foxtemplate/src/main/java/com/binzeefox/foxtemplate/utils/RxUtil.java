package com.binzeefox.foxtemplate.utils;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 一些RxJava中的工具
 * @author binze
 * 2019/11/19 9:44
 */
public class RxUtil {

    /**
     * 线程控制
     *
     * IO线程运行，主线程观察
     * @param <T> 返回的类型
     */
    public static <T> ObservableTransformer<T, T> setThread() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
