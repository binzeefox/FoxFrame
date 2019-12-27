package com.binzeefox.foxtemplate.tools.phone;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.binzeefox.foxtemplate.core.FoxCore;

import java.security.PublicKey;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

/**
 * 用于获取手机各种状态的工具类
 */
public class PhoneStatusUtil {
    private Context mCtx = FoxCore.getApplication();

    /**
     * 静态获取
     *
     * @author binze 2019/12/11 14:16
     */
    public static PhoneStatusUtil get() {
        return new PhoneStatusUtil(null);
    }

    /**
     * 初始化
     *
     * @author binze 2019/12/11 14:16
     */
    public PhoneStatusUtil(@Nullable Context context) {
        if (context != null) mCtx = context.getApplicationContext();
    }

    /**
     * 获取网络状态
     *
     * @return 是否可用
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;

        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return activeNetworkInfo.isAvailable();
        return false;
    }

    /**
     * 获取网络是否已经连接
     *
     * @return 是否链接
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public boolean isNetWorkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public void registerNetworkListener(ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager manager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.registerDefaultNetworkCallback(callback);
    }

    /**
     * 注销网络状态监听器
     *
     * @author binze 2019/11/5 12:02
     */
    public void unregisterNetworkListener(ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager manager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.unregisterNetworkCallback(callback);
    }

    /**
     * 判断GPS是否开启
     *
     * @author binze 2019/11/5 12:02
     */
    public boolean isGPSEnabled() {
        LocationManager manager = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
        return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 获取可用内存kb值
     *
     * @return 单位kb
     */
    public long getFreeMemKB() {
        ActivityManager manager = (ActivityManager) mCtx
                .getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        if (manager == null) return -1;
        manager.getMemoryInfo(info);
        return info.availMem / 1024;
    }

    /**
     * 显示软键盘
     *
     * @author binze 2019/12/26 16:27
     */
    public void showSoftKeyborad(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    mCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
