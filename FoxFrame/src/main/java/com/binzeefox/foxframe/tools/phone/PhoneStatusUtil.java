package com.binzeefox.foxframe.tools.phone;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.tools.dev.LogUtil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 用于获取手机各种状态的工具类
 */
public class PhoneStatusUtil {
    private static final String TAG = "PhoneStatusUtil";
    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_DATA = 1;
    public static final int NETWORK_WIFI = 2;

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
        if (!isNetworkAvailable() || !isNetWorkConnected()) return NETWORK_NONE;
        ConnectivityManager manager = (ConnectivityManager)
                mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return NETWORK_NONE;
        Network network = manager.getActiveNetwork();
        if (network == null) return NETWORK_NONE;
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        if (capabilities == null) return NETWORK_NONE;
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            return NETWORK_DATA;   //移动网络
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            return NETWORK_WIFI;   //Wifi
        return NETWORK_NONE;
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

    /**
     * ip工具
     *
     * @author 狐彻 2020/09/12 9:55
     */
    public IPConfig ipConfig(){
        return new IPConfig();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////////////////////////

    /**
     * IP相关
     *
     * @author 狐彻 2020/09/12 9:54
     */
    public static class IPConfig{
        private static final String TAG = "IPConfig";
        private IPConfig(){}

        /**
         * 获取IP地址
         *
         * @author 狐彻 2020/09/12 10:49
         */
        @RequiresPermission(allOf = {Manifest.permission.ACCESS_NETWORK_STATE
                , Manifest.permission.ACCESS_WIFI_STATE})
        public String getIPAddress(){
            int netState = PhoneStatusUtil.get().getNetworkState();
            if (netState == NETWORK_NONE) return null;
            if (netState == NETWORK_DATA) return getDataIPAddress();
            if (netState == NETWORK_WIFI) return getWifiIPAddress();
            return null;
        }

        /**
         * 字符串转ip
         *
         * @author 狐彻 2020/09/12 10:53
         */
        public static long ipToInt(String ipStr) {
            String[] ip = ipStr.split("\\.");
            return (Integer.parseInt(ip[0]) << 24) + (Integer.parseInt(ip[1]) << 16) + (Integer.parseInt(ip[2]) << 8) + Integer.parseInt(ip[3]);
        }

        /**
         * ip转字符
         *
         * @author 狐彻 2020/09/12 10:53
         */
        public static String intToIp(int intIp) {
            return (intIp >> 24) + "." +
                    ((intIp & 0x00FFFFFF) >> 16) + "." +
                    ((intIp & 0x0000FFFF) >> 8) + "." +
                    (intIp & 0x000000FF);
        }

        /**
         * 获取数据移动的IP地址
         *
         * @author 狐彻 2020/09/12 10:50
         */
        private static String getDataIPAddress() {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                LogUtil.e(TAG, "getDataIPAddress: ", e);
            }
            return null;
        }

        /**
         * 获取wifi的IP地址
         *
         * @author 狐彻 2020/09/12 10:50
         */
        @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
        private static String getWifiIPAddress() {
            WifiManager manager = (WifiManager) FoxCore.getApplication()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            int ip = info.getIpAddress();
            return intToIp(ip);
        }
    }
}
