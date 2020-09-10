package com.binzeefox.foxframe.tools.dev;

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
    public static <T> ObservableTransformer<T, T> setThreadIO() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 线程控制
     *
     * IO线程运行，主线程观察
     * @param <T> 返回的类型
     */
    public static <T> io.reactivex.rxjava3.core.ObservableTransformer<T, T> setThreadIO3() {
        return new io.reactivex.rxjava3.core.ObservableTransformer<T, T>() {
            @Override
            public io.reactivex.rxjava3.core.ObservableSource<T> apply(io.reactivex.rxjava3.core.Observable<T> upstream) {
                return upstream
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 线程控制
     *
     * 计算线程运行，主线程观察
     * @param <T> 返回的类型
     */
    public static <T> ObservableTransformer<T, T> setThreadComputation() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 线程控制
     *
     * 计算线程运行，主线程观察
     * @param <T> 返回的类型
     */
    public static <T> io.reactivex.rxjava3.core.ObservableTransformer<T, T> setThreadComputation3() {
        return new io.reactivex.rxjava3.core.ObservableTransformer<T, T>() {
            @Override
            public io.reactivex.rxjava3.core.ObservableSource<T> apply(io.reactivex.rxjava3.core.Observable<T> upstream) {
                return upstream
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.computation())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 线程控制
     *
     * 新线程运行，主线程观察
     * @param <T> 返回的类型
     */
    public static <T> ObservableTransformer<T, T> setThreadNew() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 线程控制
     *
     * 新线程运行，主线程观察
     * @param <T> 返回的类型
     */
    public static <T> io.reactivex.rxjava3.core.ObservableTransformer<T, T> setThreadNew3() {
        return new io.reactivex.rxjava3.core.ObservableTransformer<T, T>() {
            @Override
            public io.reactivex.rxjava3.core.ObservableSource<T> apply(io.reactivex.rxjava3.core.Observable<T> upstream) {
                return upstream
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.newThread())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread());
            }
        };
    }
}
