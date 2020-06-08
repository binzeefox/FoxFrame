package com.binzeefox.foxframe.tools.phone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;

import com.binzeefox.foxframe.core.FoxCore;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * 通过Intent隐式调用各个弹窗的方法
 *
 * @author binze
 * 2019/12/26 15:59
 */
public class IntentUtil {
    private Context mCtx = FoxCore.getApplication();

    /**
     * 静态获取
     *
     * @author binze 2019/12/11 14:16
     */
    public static IntentUtil get() {
        return new IntentUtil(null);
    }

    /**
     * 初始化
     *
     * @author binze 2019/12/11 14:16
     */
    public IntentUtil(@Nullable Context context) {
        if (context != null) mCtx = context.getApplicationContext();
    }

    /**
     * 网络设置弹窗
     *
     * @author binze 2019/12/24 12:05
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void showInternetSetting() {
        mCtx.startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
    }

    /**
     * NFC设置弹窗
     *
     * @author binze 2019/12/24 12:05
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void showNFCSetting() {
        mCtx.startActivity(new Intent(Settings.Panel.ACTION_NFC));
    }

    /**
     * 音量设置弹窗
     *
     * @author binze 2019/12/24 12:05
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void showVolumeSetting() {
        mCtx.startActivity(new Intent(Settings.Panel.ACTION_VOLUME));
    }

    /**
     * WIFI设置弹窗
     *
     * @author binze 2019/12/24 12:05
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void showWifiSetting() {
        mCtx.startActivity(new Intent(Settings.Panel.ACTION_WIFI));
    }

    /**
     * 分享弹窗
     *
     * @author binze 2019/12/26 15:59
     */
    public SendHelper shareTo() {
        return new SendHelper();
    }

    /**
     * 分享图片
     * @author binze 2019/12/26 16:18
     */
    public void shareImageTo(Uri uri, String title){
        Intent info = new Intent(Intent.ACTION_SEND);
        info.putExtra(Intent.EXTRA_STREAM, uri);
        info.setType("image/*");
        mCtx.startActivity(Intent.createChooser(info, title));
    }

    /**
     * 分享多图片
     * @author binze 2019/12/26 16:18
     */
    public void shareImageTo(ArrayList<Uri> imageUriList, String title){
        Intent info = new Intent(Intent.ACTION_SEND_MULTIPLE);
        info.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUriList);
        info.setType("image/*");
        mCtx.startActivity(Intent.createChooser(info, title));
    }

    /**
     * 分享帮助类
     * @author binze 2019/12/26 16:12
     */
    public final class SendHelper {
        private Intent info = new Intent();

        private SendHelper() {
            info.setAction(Intent.ACTION_SEND);
        }

        /**
         * 数据,先随便写几个
         * @author binze 2019/12/26 16:12
         */
        public SendHelper putExtra(String name, Object value) {
            if (value instanceof Integer)
                info.putExtra(name, (Integer) value);
            if (value instanceof Integer[])
                info.putExtra(name, (Integer[]) value);
            if (value instanceof Long)
                info.putExtra(name, (Long) value);
            if (value instanceof Long[])
                info.putExtra(name, (Long[]) value);
            if (value instanceof Byte[])
                info.putExtra(name, (Byte[]) value);
            if (value instanceof Parcelable)
                info.putExtra(name, (Parcelable) value);
            if (value instanceof Parcelable[])
                info.putExtra(name, (Parcelable[]) value);
            if (value instanceof String)
                info.putExtra(name, (String) value);
            if (value instanceof String[])
                info.putExtra(name, (String[]) value);
            return this;
        }

        /**
         * 设置类别
         * @author binze 2019/12/26 16:13
         */
        public SendHelper setType(String type){
            info.setType(type);
            return this;
        }

        /**
         * 完成
         * @author binze 2019/12/26 16:13
         */
        public void commit(String title){
            mCtx.startActivity(Intent.createChooser(info, title));
        }
    }
}
