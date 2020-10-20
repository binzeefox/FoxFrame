package com.binzeefox.foxframe.core.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.binzeefox.foxframe.tools.dev.LogUtil;

/**
 * 跳转器
 *
 * @author binze 2019/12/10 12:38
 */
public class Navigator {
    private static final String TAG = "Navigator";
    public static final String PARAMS_BUNDLE = "PARAMS_BUNDLE"; //跳转参数键

    private Intent intent;
    private final Commitor commitor;

    /**
     * 静态方法，获取跳转参数Bundle
     *
     * @author binze 2019/12/10 12:56
     */
    public static Bundle getDataFromNavigate(Intent intent) {
        Bundle bundle;
        if (intent == null) return new Bundle();
        bundle = intent.getBundleExtra(PARAMS_BUNDLE);
        if (bundle == null) return new Bundle();
        return bundle;
    }

    /**
     * 碎片构造器(隐式跳转)
     *
     * @author binze 2019/12/10 13:49
     */
    public Navigator(Fragment fragment, Intent intent) {
        this.commitor = new FragmentCommitor(fragment);
        this.intent = intent;
    }

    /**
     * 活动构造器(隐式跳转)
     *
     * @author binze 2019/12/10 13:49
     */
    public Navigator(Activity activity, Intent intent) {
        this.commitor = new ActivityCommitor(activity);
        this.intent = intent;
    }

    /**
     * 碎片构造器
     *
     * @author binze 2019/12/10 13:49
     */
    public Navigator(Fragment fragment, Class<? extends Activity> target) {
        this.commitor = new FragmentCommitor(fragment);
        this.intent = new Intent(fragment.getContext(), target);
    }

    /**
     * 活动构造器
     *
     * @author binze 2019/12/10 13:49
     */
    public Navigator(Activity activity, Class<? extends Activity> target) {
        this.commitor = new ActivityCommitor(activity);
        this.intent = new Intent(activity, target);
    }

    /**
     * 设置参数
     *
     * @author binze 2019/12/10 12:44
     */
    public Navigator setParams(@NonNull Bundle bundle) {
        intent.putExtra(PARAMS_BUNDLE, bundle);
        return this;
    }

    /**
     * 直接设置intent
     *
     * @author binze 2019/12/10 12:48
     */
    public Navigator configIntent(ConfigMethod method) {
        method.config(intent);
        return this;
    }

    /**
     * 跳转
     * @author binze 2019/12/10 13:53
     */
    public void commit() {
        commitor.commit();
    }

    /**
     * 跳转
     * @author binze 2019/12/10 13:53
     */
    public void commit(Bundle options) {
        commitor.commit(options);
    }

    /**
     * 请求跳转
     * @author binze 2019/12/10 13:54
     */
    public void commitForResult(int requestCode) {
        commitor.commitForResult(requestCode);
    }

    /**
     * 请求跳转
     * @author binze 2019/12/10 13:54
     */
    public void commitForResult(int requestCode, Bundle options) {
        commitor.commitForResult(requestCode, options);
    }

    /**
     * 直接设置intent回调
     *
     * @author binze 2019/12/10 12:46
     */
    public interface ConfigMethod {
        void config(Intent intent);
    }

    /**
     * 执行器接口
     *
     * @author binze 2019/12/10 13:23
     */
    private interface Commitor {
        void commit();

        void commit(Bundle options);

        void commitForResult(int requestCode);

        void commitForResult(int requestCode, Bundle options);
    }

    /**
     * 活动执行器
     *
     * @author binze 2019/12/10 13:40
     */
    private class ActivityCommitor implements Commitor {
        final Activity activity;

        ActivityCommitor(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void commit() {
            activity.startActivity(intent);
        }

        @Override
        public void commit(Bundle options) {
            activity.startActivity(intent, options);
        }

        @Override
        public void commitForResult(int requestCode) {
            activity.startActivityForResult(intent, requestCode);
        }

        @Override
        public void commitForResult(int requestCode, Bundle options) {
            activity.startActivityForResult(intent, requestCode, options);
        }
    }

    /**
     * 碎片执行器
     *
     * @author binze 2019/12/10 13:42
     */
    private class FragmentCommitor implements Commitor {
        final Fragment fragment;

        FragmentCommitor(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void commit() {
            fragment.startActivity(intent);
        }

        @Override
        public void commit(Bundle options) {
            fragment.startActivity(intent, options);
        }

        @Override
        public void commitForResult(int requestCode) {
            Activity activity = fragment.getActivity();
            if (activity == null) {
                LogUtil.e(TAG, "commitForResult: Fragment未绑定Activity");
                return;
            }
            ActivityCompat.startActivityForResult(fragment.getActivity(), intent, requestCode, null);
        }

        @Override
        public void commitForResult(int requestCode, Bundle options) {
            Activity activity = fragment.getActivity();
            if (activity == null) {
                LogUtil.e(TAG, "commitForResult: Fragment未绑定Activity");
                return;
            }
            ActivityCompat.startActivityForResult(fragment.getActivity(), intent, requestCode, options);
        }
    }
}
