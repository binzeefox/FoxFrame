package com.binzeefox.foxframe.core;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.binzeefox.foxframe.core.tools.DataHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 框架核心
 *
 * @author binze
 * 2019/12/10 10:58
 */
public class FoxCore {
    private static final String TAG = "FoxCore";
    private static FoxCore sInstance;   //单例
    private static Application mApp;   //绑定的Application

//    private Map<String, Object> globalData = new HashMap<>(); //全局数据
    private final DataHolder globalData = new DataHolder(); //2020/06/10 11:20 修改为final类型

    private Stack<AppCompatActivity> activityStack = new Stack<>();  //返回栈

    /**
     * 静态初始化
     *
     * @author binze 2019/12/10 11:01
     */
    public static FoxCore init(Application application) {
        sInstance = new FoxCore(application);
        return sInstance;
    }

    /**
     * 静态获取
     *
     * @author binze 2019/12/10 11:04
     */
    public synchronized static FoxCore get() {
        if (sInstance == null) {
            Throwable t = new IllegalAccessException("should call init(Application) first!!!");
            Log.e(TAG, "get: 获取失败", t);
            return null;
        }
        return sInstance;
    }

    /**
     * 私有化构造器
     *
     * @author binze 2019/12/10 11:04
     */
    private FoxCore(Application application) {
        mApp = application;
    }

    /**
     * 获取Application实例
     *
     * @author binze 2019/12/10 11:05
     */
    public static Application getApplication() {
        return mApp;
    }

    /**
     * 获取Package信息
     *
     * @author binze 2019/12/10 11:12
     */
    public PackageInfo getPackageInfo() {
        try {
            PackageManager manager = mApp.getPackageManager();
            return manager.getPackageInfo(mApp.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getPackageInfo: 获取失败", e);
        }
        return null;
    }

    /**
     * 获取版本名
     *
     * @author binze 2019/12/10 11:15
     */
    public String getVersionName() {
        PackageInfo info = getPackageInfo();
        if (info == null) {
            Log.e(TAG, "getVersionName: 获取版本名失败");
            return null;
        }
        return info.versionName;
    }

    /**
     * 获取版本号
     *
     * @return 若获取失败则返回 -1
     * @author binze 2019/12/10 11:17
     */
    public long getVersionCode() {
        PackageInfo info = getPackageInfo();
        if (info == null) {
            Log.e(TAG, "getVersionName: 获取版本号失败");
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return info.getLongVersionCode();
        } else return info.versionCode;
    }

    /**
     * 存入全局数据，不建议存入大量数据，会造成OOM
     *
     * @author binze 2019/12/10 11:26
     */
    public void putGlobalData(String key, Object data) {
        globalData.set(key, data);
    }

    /**
     * 取出数据，若转类型失败则会打印错误，并返回null
     *
     * @author binze 2019/12/10 11:33
     */
    public <T> T getGlobalData(String key) {
        return getGlobalData(key, null);
    }

    /**
     * 取出数据
     *
     * @param defaultValue 默认值
     * @author binze 2019/12/10 11:33
     */
    public <T> T getGlobalData(String key, T defaultValue) {
        return globalData.read(key, defaultValue);
    }

    /**
     * 订阅全局数据
     * @author binze 2020/6/8 10:00
     */
    public void submitGlobalData(String key, DataHolder.Callback callback){
        globalData.submit(key, callback);
    }

    /**
     * 取消订阅全局数据
     * @author binze 2020/6/8 10:01
     */
    public void unSubmitGlobalData(String key){
        globalData.unsubmit(key);
    }

    /**
     * 移除全局数据
     *
     * @return 被移除的数据
     * @author binze 2019/12/10 11:35
     */
    public Object removeGlobalData(String key) {
        return globalData.remove(key);
    }

    /* 2020/06/10 11:50 */
    /* 返回栈功能比较鸡肋，还影响性能，决定移除掉 */
    /* 2020/06/16 14:40 */
    /* 返回栈功能被应用于部分方法。不能删去 */

    /**
     * 压入返回栈
     * @author binze 2019/12/10 11:41
     */
    public void pushActivity(AppCompatActivity activity){
        activityStack.push(activity);
    }

    /**
     * 获取顶部Activity
     * @author binze 2019/12/10 11:42
     */
    public AppCompatActivity getTopActivity(){
        return activityStack.peek();
    }

    /**
     * Activity弹栈
     * @author binze 2019/12/10 11:43
     */
    public void popActivity(){
        activityStack.pop().finish();
    }

    /**
     * 杀死一定数量的Activity
     * @author binze 2019/12/11 13:53
     */
    public void killActivity(int count){
        while (count > 0){
            popActivity();
            count--;
        }
    }

    /**
     * Activity移除
     * @author binze 2019/12/10 11:43
     */
    public void removeActivity(AppCompatActivity activity){
        activityStack.remove(activity);
    }

    /**
     * 获取当前Activity列表
     * @author binze 2019/12/10 11:43
     */
    public List<AppCompatActivity> getActivityList(){
        return new ArrayList<>(activityStack);
    }
}
