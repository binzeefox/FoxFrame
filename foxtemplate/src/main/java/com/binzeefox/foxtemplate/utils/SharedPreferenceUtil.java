package com.binzeefox.foxtemplate.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.binzeefox.foxtemplate.base.FoxApplication;


/**
 * SharedPreference工具
 *
 * @author binze
 * 2019/9/29 10:30
 */
public class SharedPreferenceUtil {
    public static final String FILENAME_CONFIG = "config";  //预设的配置文件 文件名
    public static final String CONFIG_LAST_USER = "last_login"; //预设的上次登录用户字段名

    private static SharedPreferenceUtil sInstance;
    private Application mContext;

    public static SharedPreferenceUtil get(){
        if (sInstance == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreferenceUtil();
                }
            }
        }
        return sInstance;
    }

    /**
     * 私有化构建
     */
    private SharedPreferenceUtil(){
        mContext = FoxApplication.get();
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
     * @param defaultKey    默认返回值
     * @return  值
     */
    public synchronized String readString(String fileName, String key, String defaultKey){
        SharedPreferences sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getString(key, defaultKey);
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
     * 读取Config信息
     * @param key   键
     * @return  信息
     */
    public synchronized String readServerConfig(String key){
        return readString(FILENAME_CONFIG, key, "");
    }

    /**
     * 写入Config信息
     * @param key   键
     * @param value  信息
     */
    public synchronized void writeServerConfig(String key, String value){
        writeString(FILENAME_CONFIG, key, value);
    }

    /**
     * 示例代码：写入最后登录用户名
     * @author binze 2019/11/19 9:40
     */
    public static void saveLastLoginUser(String username){
        get().writeServerConfig(CONFIG_LAST_USER, username);
    }

    /**
     * 示例代码：读取最后登录用户名
     *
     * @return 最后登录的用户名，若不存在则返回 ""
     * @author binze 2019/11/19 9:41
     */
    public static String getLastLoginUser(){
        return get().readServerConfig(CONFIG_LAST_USER);
    }
}
