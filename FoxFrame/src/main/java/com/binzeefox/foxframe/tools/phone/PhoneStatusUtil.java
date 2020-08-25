package com.binzeefox.foxframe.tools.phone;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.binzeefox.foxframe.core.FoxCore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

/**
 * 用于获取手机各种状态的工具类
 */
public class PhoneStatusUtil {
    private static final String TAG = "PhoneStatusUtil";
    private Context mCtx;

    /**
     * 静态获取
     *
     * @author binze 2019/12/11 14:16
     */
    public static PhoneStatusUtil get() {
        return new PhoneStatusUtil(FoxCore.getApplication());
    }

    /**
     * 初始化
     *
     * @author binze 2019/12/11 14:16
     */
    public PhoneStatusUtil(@NonNull Context context) {
        mCtx = context.getApplicationContext();
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
        if (!isNetworkAvailable()) return false;
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
     * 获取网络状态
     * @author binze 2020/8/25 11:42
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public int getNetworkState(){
        if (!isNetworkAvailable() || !isNetWorkConnected()) return 0;
        ConnectivityManager manager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return 0;
        Network network = manager.getActiveNetwork();
        if (network == null) return 0;
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        if (capabilities == null) return 0;
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            return 1;   //移动网络
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            return 2;   //Wifi
        return 0;
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
        if (manager != null) {
            manager.registerDefaultNetworkCallback(callback);
        } else Log.e(TAG, "registerNetworkListener: 注册网络状态失败，没有网络连接" );
    }

    /**
     * 注销网络状态监听器
     *
     * @author binze 2019/11/5 12:02
     */
    public void unregisterNetworkListener(ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager manager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            manager.unregisterNetworkCallback(callback);
        } else Log.e(TAG, "unregisterNetworkListener: 注册网络状态失败，没有网络连接" );
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
    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    mCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
