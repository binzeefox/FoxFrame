package com.binzeefox.foxtemplate.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.LruCache;

/**
 * 缓存工具类
 *
 * @deprecated 未完成
 * @author binze
 * 2019/12/2 15:28
 */
public class CacheUtil {
    private final LruCache<String, Object> mLruCache;

    public CacheUtil(Context ctx){
        ActivityManager am = (ActivityManager)
                ctx.getSystemService(Context.ACTIVITY_SERVICE);
        int availMenInBytes = am.getMemoryClass() * 1024 *1024;
        mLruCache = new LruCache<>(availMenInBytes / 8);
    }


}
