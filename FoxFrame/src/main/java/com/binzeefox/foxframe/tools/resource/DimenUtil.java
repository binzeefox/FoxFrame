package com.binzeefox.foxframe.tools.resource;

import android.content.Context;
import android.content.res.Resources;

import com.binzeefox.foxframe.core.FoxCore;

import androidx.annotation.NonNull;


/**
 * 数值工具类，主要完成数值转换功能
 * @author binze
 * 2019/11/19 9:26
 */
public class DimenUtil {
    private final float scale;

    /**
     * 静态获取
     * @author binze 2019/12/11 14:05
     */
    public static DimenUtil get(){
        return new DimenUtil(FoxCore.getApplication());
    }

    /**
     * 构造器
     * @author binze 2019/12/11 14:04
     */
    public DimenUtil(@NonNull Context context){
        scale = context.getResources().getDisplayMetrics().density;
    }

    /**
     * 通过dp获取px
     * @deprecated 计算不准确，改用{{@link #dipToPx(int)}}
     */
    public static int dipToPx(float dpValue) {
        int density = (int) Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * density);
    }

    /**
     * 通过px获取dp
     * @deprecated 计算不准确，改用{{@link #pxToDip(int)}}
     */
    public static int pxToDip(float pxValue) {
        int density = (int) Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / density);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public float dipToPx(int dpValue) {
        return dpValue * scale + 0.5f;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public float pxToDip(int pxValue) {
        return pxValue / scale + 0.5f;
    }
}
