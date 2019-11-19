package com.binzeefox.foxtemplate.wrappers;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 为了更简洁。可以根据需要选择想实现的方法
 * @author binze 2019/11/19 9:43
 */
public class ObserverWrapper<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
