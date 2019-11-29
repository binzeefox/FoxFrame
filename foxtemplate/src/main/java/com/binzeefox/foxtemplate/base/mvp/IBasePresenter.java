package com.binzeefox.foxtemplate.base.mvp;

/**
 * P层业务接口的一个基类，封装了一个回收方法，方便在销毁V层时对P层进行一些回收工作
 * @author binze
 */
public interface IBasePresenter {
    void finish();  //回收
}
