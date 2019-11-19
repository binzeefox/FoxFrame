package com.binzeefox.foxtemplate.base.mvp;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BasePresenter<T extends IBaseView> implements IBasePresenter {
    protected T view;
    protected CompositeDisposable dContainer;

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
     *
     * @param view
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
