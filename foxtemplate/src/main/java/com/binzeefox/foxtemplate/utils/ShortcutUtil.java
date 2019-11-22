package com.binzeefox.foxtemplate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.binzeefox.foxtemplate.base.FoxApplication;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 快捷方式工具类
 *
 * 长按图标后显示的捷径工具类，动态生成的结晶也可以一直存在。
 * 无需静态配置，当然静态配置好也可以
 *
 * @author binze
 * 2019/11/22 12:02
 */
public class ShortcutUtil {
    private static final String TAG = "ShortcutUtil";
    public static final String PARAMS_EXTRA = "params_extra";

    private Context mCtx = FoxApplication.get();    //上下文
    private ShortcutManager mManager;   //服务器

    /**
     * 静态获取
     *
     * @author binze 2019/11/22 12:02
     */
    public static ShortcutUtil get() {
        return new ShortcutUtil();
    }

    /**
     * 静态生成捷径Intent
     *
     * @param mCtx     发起源活动
     * @param activity 目标活动
     * @param bundle   携带数据
     * @author binze 2019/11/22 13:41
     */
    public static Intent getInfoIntent(Activity mCtx, Class<? extends Activity> activity, Bundle bundle) {
        Intent intent = new Intent(mCtx, activity);
        intent.putExtra(PARAMS_EXTRA, bundle);
        intent.setAction(Intent.ACTION_VIEW);
        return intent;
    }

    /**
     * 构造器
     *
     * @author binze 2019/11/22 12:02
     */
    public ShortcutUtil() {
        mManager = mCtx.getSystemService(ShortcutManager.class);
    }

    /**
     * 获取管理器
     *
     * @author binze 2019/11/22 13:56
     */
    public ShortcutManager getManager() {
        return mManager;
    }

    /**
     * 获取动态快捷方式
     *
     * @author binze 2019/11/22 12:06
     */
    public List<ShortcutInfo> getDynamicShortcuts() {
        return mManager.getDynamicShortcuts();
    }

    /**
     * 添加动态快捷方式
     *
     * @author binze 2019/11/22 12:09
     */
    public boolean addDynamicShortcuts(List<ShortcutInfo> infoList) {
        if (getDynamicShortcuts().size() + infoList.size() > mManager.getMaxShortcutCountPerActivity()) {
            //快捷方式容量满了
            Log.e(TAG, "setDynamicShortcut: 无法添加快捷方式，已达上限");
            return false;
        }
        mManager.addDynamicShortcuts(infoList);
        return true;
    }

    /**
     * 设置动态快捷方式
     *
     * @author binze 2019/11/22 12:15
     */
    public boolean setDynamicShortcuts(List<ShortcutInfo> infoList) {
        if (getDynamicShortcuts().size() + infoList.size() > mManager.getMaxShortcutCountPerActivity()) {
            //快捷方式容量满了
            Log.e(TAG, "setDynamicShortcut: 无法添加快捷方式，已达上限");
            return false;
        }
        mManager.setDynamicShortcuts(infoList);
        return true;
    }

    /**
     * 禁用快捷方式
     *
     * @param items   被禁用的快捷方式id
     * @param message 点击被禁用按钮的提示
     * @author binze 2019/11/22 12:26
     */
    public void disableShortcuts(@NonNull List<String> items, String message) {
        mManager.disableShortcuts(items, message);
    }

    /**
     * 移除快捷方式
     *
     * @param items 被禁用的快捷方式id
     * @author binze 2019/11/22 12:26
     */
    public void removeShortcuts(@NonNull List<String> items) {
        mManager.removeDynamicShortcuts(items);
    }

    /**
     * 移除所有捷径
     *
     * @author binze 2019/11/22 13:55
     */
    public void removeAllShortcuts() {
        mManager.removeAllDynamicShortcuts();
    }

    /**
     * 更新
     *
     * @author binze 2019/11/22 12:33
     */
    public void updateShortcuts(@NonNull List<ShortcutInfo> items) {
        mManager.updateShortcuts(items);
    }

    /**
     * 获取特定ID的快捷方式
     *
     * @author binze 2019/11/22 12:35
     */
    public ShortcutInfo getShortcut(String id) {
        if (TextUtils.isEmpty(id)) return null;
        for (ShortcutInfo info : getDynamicShortcuts()) {
            if (id.equals(info.getId())) return info;
        }
        return null;
    }
}
