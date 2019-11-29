package com.binzeefox.foxtemplate.base.mvp;

import io.reactivex.disposables.CompositeDisposable;

/**
 * MVP P层基类
 *
 * @param <T>   V层的泛型
 * @author binze
 */
public abstract class BasePresenter<T extends IBaseView> implements IBasePresenter {
    protected T view;   //V层
    protected CompositeDisposable dContainer;   //V层提供的Rx回收器

    /**
     * 初始化
     *
     * @param view       V层
     * @param dContainer Rx回收器
     */
    public BasePresenter(T view, CompositeDisposable dContainer) {
        onBind(view);
        this.dContainer = dContainer;
    }

    /**
     * 绑定View
     */
    private void onBind(T view) {
        this.view = view;
    }

    /**
     * 回收
     */
    @Override
    public void finish() {
        view = null;
    }
}
