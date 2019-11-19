package com.binzeefox.foxtemplate.utils;

import android.content.res.Resources;

import com.binzeefox.foxtemplate.base.FoxApplication;

/**
 * 数值工具类，主要完成数值转换功能
 * @author binze
 * 2019/11/19 9:26
 */
public class DimenUtil {

    /**
     * 通过dp获取px
     * @deprecated 计算不准确，改用{{@link #dip2px(int)}}
     */
    public static int dip2px(float dpValue) {
        int density = (int) Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * density);
    }

    /**
     * 通过px获取dp
     * @deprecated 计算不准确，改用{{@link #px2dip(int)}}
     */
    public static int px2dip(float pxValue) {
        int density = (int) Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / density);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(int dpValue) {
        final float scale = FoxApplication.get().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(int pxValue) {
        final float scale = FoxApplication.get().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
