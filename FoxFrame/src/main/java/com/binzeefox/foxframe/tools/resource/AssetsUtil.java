package com.binzeefox.foxframe.tools.resource;

import android.content.Context;

import com.binzeefox.foxframe.core.FoxCore;

/**
 * 操作Assets文件工具类
 *
 * @author binze
 * 2020/1/2 14:38
 */
public class AssetsUtil {
    private static final String TAG = "AssetsUtil";
    private Context mCtx;

    /**
     * 构造器
     * @author binze 2020/1/2 14:42
     */
    public AssetsUtil(Context context){
        mCtx = context.getApplicationContext();
    }

    /**
     * 静态获取
     * @author binze 2020/1/2 14:42
     */
    public static AssetsUtil get(){
        return new AssetsUtil(FoxCore.getApplication());
    }
}
