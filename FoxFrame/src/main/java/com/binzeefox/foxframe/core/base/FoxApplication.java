package com.binzeefox.foxframe.core.base;

import android.annotation.SuppressLint;
import android.app.Application;

import com.binzeefox.foxframe.core.FoxCore;

/**
 * 自定义Application
 * @author binze
 * 重构自 2019/12/9 10:28
 */
@SuppressLint("Registered")
public abstract class FoxApplication extends Application {
    private static final String TAG = "FoxApplication";
    public static FoxCore Core; //核心

    @Override
    public void onCreate() {
        super.onCreate();
        Core = FoxCore.init(this);
    }

    /**
     * 获取内容提供者授权信息, 默认为空
     * @author binze 2019/12/11 11:48
     */
    public String getAuthority(){
        return null;
    }
}
