package com.binzeefox.foxframe.tools.resource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.binzeefox.foxframe.core.FoxCore;

import androidx.annotation.NonNull;


/**
 * SharedPreference工具
 *
 * @author binze
 * 2019/9/29 10:30
 */
public class SharedPreferenceUtil {

    @SuppressLint("StaticFieldLeak")
    private static SharedPreferenceUtil sInstance;
    private final Context mContext;   //Application context

    public static SharedPreferenceUtil get(){
        if (sInstance == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreferenceUtil(FoxCore.getApplication());
                }
            }
        }
        return sInstance;
    }

    public static SharedPreferenceUtil get(@NonNull Context context){
        if (sInstance == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreferenceUtil(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * 私有化构建
     */
    protected SharedPreferenceUtil(Context context){
        mContext = context.getApplicationContext();
    }

    /**
     * 读取字符串数据
     * @param fileName 文件名
     * @param key   键
     * @return  值
     */
    public synchronized String readString(String fileName, String key) {
        return readString(fileName, key, null);
    }

    /**
     * 读取字符串数据
     * @param fileName 文件名
     * @param key   键
     * @param defaultValue    默认返回值
     * @return  值
     */
    public synchronized String readString(String fileName, String key, String defaultValue){
        SharedPreferences sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    /**
     * 写入字符串数据
     * @param fileName  文件名
     * @param key   键
     * @param value 值
     */
    public synchronized void writeString(String fileName, String key, String value){
        SharedPreferences.Editor editor =
                mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit();

        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 读取字符串数据
     * @param fileName 文件名
     * @param key   键
     * @param defaultValue    默认返回值
     * @return  值
     */
    public synchronized int readInteger(String fileName, String key, int defaultValue){
        SharedPreferences sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }

    /**
     * 写入字符串数据
     * @param fileName  文件名
     * @param key   键
     * @param value 值
     */
    public synchronized void writeInteger(String fileName, String key, int value){
        SharedPreferences.Editor editor =
                mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit();

        editor.putInt(key, value);
        editor.apply();
    }
}
