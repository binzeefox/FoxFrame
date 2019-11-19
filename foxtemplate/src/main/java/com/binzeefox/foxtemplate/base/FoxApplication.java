package com.binzeefox.foxtemplate.base;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.binzeefox.foxtemplate.utils.ActivityCollector;

//import org.litepal.LitePal;


/**
 * 自定义Application
 * <p>
 * 集成自定义的Activity管理器，内涵模拟返回栈
 * 可以批量销毁activity
 */
public abstract class FoxApplication extends Application {
    private static final String TAG = "FoxApplication";

    public static final boolean DEV_MODE = true; //开发模式

    private static FoxApplication sInstance;
    // 自定义Activity管理器
    private final static ActivityCollector mCollector = ActivityCollector.get();

    /**
     * 静态获取Application实例
     * @author binze 2019/11/1 12:02
     */
    public static FoxApplication get() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    /**
     * 获取当前app信息
     * @author binze 2019/11/1 12:06
     */
    public static PackageInfo getPackageInfo(){
        try {
            PackageManager manager = get().getPackageManager();
            return manager.getPackageInfo(get().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取版本名
     * @author binze 2019/11/1 12:06
     */
    public static String getVersionName(){
        PackageInfo info = getPackageInfo();
        return info == null ? null : info.packageName;
    }

    /**
     * 获取版本号
     *
     * @return 若获取失败则返回 -1
     * @author binze 2019/11/1 12:06
     */
    public static long getVersionCode(){
        PackageInfo info = getPackageInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return info == null ? -1 : info.getLongVersionCode();
        } else return info == null ? -1 : info.getLongVersionCode();
    }

    /**
     * 模拟返回栈注册
     */
    public void registerActivity(FoxActivity activity) {
        mCollector.list(activity);
    }

    /**
     * 模拟返回栈注销
     */
    public void unRegisterActivity(FoxActivity activity) {
        mCollector.delist(activity);
    }

    /**
     * 获取Activity管理器
     */
    public ActivityCollector getActivityCollector() {
        return mCollector;
    }

    /**
     * 获取顶部Activity
     */
    public FoxActivity getTopActivity() {
        return mCollector.getTopActivity();
    }

    /**
     * 存入全局缓存数据
     *
     * @param key   键
     * @param value 值
     */
    public static void putGlobalData(String key, Object value) {
        mCollector.putGlobalData(key, value);
    }

    /**
     * 获取全局缓存数据
     *
     * @param key 键
     * @param <T> 数据类型
     * @return 数据
     */
    public static <T> T getGlobalData(String key) {
        return mCollector.getGlobalData(key);
    }

    /**
     * 移除全局缓存数据
     *
     * @param key 键
     * @param <T> 数据类型
     * @return 数据
     */
    public static <T> T removeGlobalData(String key) {
        return mCollector.removeGlobalData(key);
    }
}
