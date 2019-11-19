package com.binzeefox.foxtemplate.base.mvp;


import com.binzeefox.foxtemplate.base.FoxActivity;

public interface IBaseView{

    //用于返回当前活动基类
    FoxActivity getBaseActivity();

    /**
     * 用于弹出提示
     * @param code 信息代码
     */
    void notice(int code);
}
