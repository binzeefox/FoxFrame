package com.binzeefox.foxtemplate.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.binzeefox.foxtemplate.base.FoxApplication;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

/**
 * 用于获取手机各种状态的工具类
 */
public class PhoneStatusUtil {

    /**
     * 获取网络状态
     *
     * @return 是否可用
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean isNetworkAvailable() {
        Context ctx = FoxApplication.get();
        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;

        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        if (activeNetworkInfo.isConnected()) return activeNetworkInfo.isAvailable();
        return false;
    }

    /**
     * 获取网络是否已经连接
     *
     * @return 是否链接
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean isNetWorkConnected() {
        Context ctx = FoxApplication.get();
        ConnectivityManager connectivityManager = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null) return false;
            return activeNetworkInfo.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 注册网络状态监听器
     *
     * @author binze 2019/11/5 12:02
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public static void registerNetworkListener(ConnectivityManager.NetworkCallback callback) {
        Context ctx = FoxApplication.get();
        ConnectivityManager manager = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.registerDefaultNetworkCallback(callback);
    }

    /**
     * 注销网络状态监听器
     *
     * @author binze 2019/11/5 12:02
     */
    public static void unregisterNetworkListener(ConnectivityManager.NetworkCallback callback) {
        Context ctx = FoxApplication.get();
        ConnectivityManager manager = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.unregisterNetworkCallback(callback);
    }

    /**
     * 判断GPS是否开启
     *
     * @author binze 2019/11/5 12:02
     */
    public static boolean isGPSEnabled() {
        Context context = FoxApplication.get();
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 获取可用内存kb值
     *
     * @return 单位kb
     */
    public static long getFreeMemKB() {
        Context ctx = FoxApplication.get();
        ActivityManager manager = (ActivityManager) ctx
                .getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        return info.availMem / 1024;
    }
}
