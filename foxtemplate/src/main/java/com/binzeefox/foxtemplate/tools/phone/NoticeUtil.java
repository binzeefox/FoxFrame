package com.binzeefox.foxtemplate.tools.phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.binzeefox.foxtemplate.core.FoxCore;
import com.binzeefox.foxtemplate.views.CustomDialogFragment;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;


/**
 * 弹窗工具类
 * <p/>
 * 单例
 * Toast工具
 * 弹窗工具
 */
public class NoticeUtil {
    private static final String TAG = "NoticeUtil";
    private static NoticeUtil mInstance;    //单例
    private CustomDialogFragment dialogHelper;  //弹窗助手
    private Context mCtx = FoxCore.getApplication();    //Application 实例
    private Toast mToast;   //Toast实例
    private FoxCore core = FoxCore.get();

    /**
     * 单例模式
     */
    public static NoticeUtil get() {
        if (mInstance != null) return mInstance;
        mInstance = new NoticeUtil(null);
        return mInstance;
    }

    /**
     * 构造器
     *
     * @param ctx ctx
     */
    private NoticeUtil(@Nullable Context ctx) {
        //获取全局ApplicationContext防止内存泄漏
        if (ctx != null) mCtx = ctx.getApplicationContext();
    }


//    ******↓Toast相关

    /**
     * 显示提示
     *
     * @param resourceId 提示文字资源
     */
    public void showToast(int resourceId) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(mCtx, resourceId, Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * 显示提示
     *
     * @param text 文字
     */
    public void showToast(CharSequence text) {
        if (TextUtils.isEmpty(text)) return;
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(mCtx, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * 获取弹窗构造器
     */
    public CustomDialogFragment getDialogHelper() {
        if (dialogHelper != null) {
            dismissDialog();
            dialogHelper = null;
        }

        dialogHelper = CustomDialogFragment.get(mCtx);
        return dialogHelper;
    }

    /**
     * 显示网络加载Dialog
     */
    public CustomDialogFragment showLoadingDialog(String title, AlertDialog.OnClickListener cancelClickListener) {
        CustomDialogFragment dialogHelper = getDialogHelper();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ProgressBar bar = new ProgressBar(mCtx);
        bar.setPadding(0, 30, 0, 30);
        bar.setLayoutParams(params);
        bar.setIndeterminate(true);
        dialogHelper
                .title(title)
                .cancelable(false)
                .view(bar);
        if (cancelClickListener != null)
            dialogHelper.negativeButton("取消", cancelClickListener);
        if (core != null)
            dialogHelper.show(core.getTopActivity().getSupportFragmentManager());
        else
            Log.e(TAG, "getHelper: call FoxCore#init(Application) first!!!", new IllegalAccessException());

        return dialogHelper;
    }

    /**
     * 显示简单的警告信息
     *
     * @author binze 2019/10/21 15:08
     * update 2019/12/17 取消自动show()
     */
    public CustomDialogFragment showSimpleAlertDialog(String title, String message, boolean cancelable
            , AlertDialog.OnClickListener positiveListener
            , AlertDialog.OnClickListener navigateListener) {

        CustomDialogFragment dialogHelper = getDialogHelper();
        dialogHelper.title(title).message(message).cancelable(cancelable);
        if (positiveListener != null) {
            dialogHelper.positiveButton("确定", positiveListener);
        }
        dialogHelper.negativeButton("取消", navigateListener);

        return dialogHelper;
    }

    /**
     * 注销弹窗
     */
    public void dismissDialog() {
        if (dialogHelper == null)
            return;
        if (dialogHelper.getFragmentManager() != null)
            dialogHelper.dismiss();
        dialogHelper = null;
    }

    /**
     * 震动功能
     * @author binze 2019/12/17 10:37
     */
    @RequiresPermission(allOf = Manifest.permission.VIBRATE)
    public Vibrator vibrate(){
        return new Vibrator();
    }

    /**
     * 震动功能类
     * @author binze 2019/12/17 10:41
     */
    public class Vibrator{
        private android.os.Vibrator vibrator = (android.os.Vibrator) mCtx.getSystemService(VIBRATOR_SERVICE);
        private int amplitude = DEFAULT_AMPLITUDE;  //振幅

        private Vibrator(){}

        /**
         * 设置震动
         * @author binze 2019/12/17 10:49
         */
        public Vibrator setAmplitude(int amplitude){
            this.amplitude = amplitude;
            return this;
        }

        /**
         * 一次性震动
         * @author binze 2019/12/17 10:48
         */
        @RequiresPermission(allOf = Manifest.permission.VIBRATE)
        public void vibrate(long period){
            VibrationEffect effect = VibrationEffect.createOneShot(period, amplitude);
            vibrator.vibrate(effect);
        }

        /**
         * 波形震动
         * @author binze 2019/12/17 10:49
         */
        @RequiresPermission(allOf = Manifest.permission.VIBRATE)
        public void vibrate(long[] timings, int[] amplitudes, int repeat){
            VibrationEffect effect = VibrationEffect.createWaveform(timings, amplitudes, repeat);
            vibrator.vibrate(effect);
        }

        /**
         * 波形震动
         * @author binze 2019/12/17 10:49
         */
        @RequiresPermission(allOf = Manifest.permission.VIBRATE)
        public void vibrate(long[] timings, int repeat){
            VibrationEffect effect = VibrationEffect.createWaveform(timings, repeat);
            vibrator.vibrate(effect);
        }
    }

    /**
     * 显示带进度条网络加载Dialog
     */
    public static class ProgressDialog {
        private static final String TAG = "ProgressDialog";
        @SuppressLint("StaticFieldLeak")
        static ProgressDialog sInstance;    //Application Context
        private CustomDialogFragment helper;
        private ProgressBar bar;
        private int max;
        private FoxCore core = FoxCore.get();

        public static ProgressDialog getInstance(String title, int max) {
            return new ProgressDialog(FoxCore.getApplication(), title, max);
        }

        public ProgressDialog(Context context, String title, int max) {
            this.max = max;
            Context ctx = context.getApplicationContext();
            helper = getHelper();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            bar.setPadding(60, 30, 60, 30);
            bar.setLayoutParams(params);
            bar.setIndeterminate(false);
            bar.setMax(max);
//            bar.setProgress(0);
            helper
                    .title(title)
                    .cancelable(false)
                    .view(bar);
        }

        public void progress(int progress) {
            if (progress > max) progress = max;
            bar.setProgress(progress, true);
        }

        public void increase(int count) {
            bar.incrementProgressBy(count);
        }

        public void show() {
            if (core != null) helper.show(core.getTopActivity().getSupportFragmentManager());
            else
                Log.e(TAG, "show: call FoxCore#init(Application) first!!!", new IllegalAccessException());
        }

        public void dismiss() {
            if (helper != null && helper.getFragmentManager() != null)
                helper.dismiss();
            helper = null;
        }

        private CustomDialogFragment getHelper() {
            if (helper != null && helper.getFragmentManager() != null)
                helper.dismiss();
            helper = null;

            if (core != null)
                helper = CustomDialogFragment.get(core.getTopActivity());
            else
                Log.e(TAG, "getHelper: call FoxCore#init(Application) first!!!", new IllegalAccessException());
            return helper;
        }
    }
}
