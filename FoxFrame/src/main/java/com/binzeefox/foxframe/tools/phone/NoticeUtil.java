package com.binzeefox.foxframe.tools.phone;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.os.VibrationEffect;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.tools.dev.ThreadUtil;
import com.binzeefox.foxframe.tools.resource.DimenUtil;
import com.binzeefox.foxframe.views.CustomDialogFragment;

import androidx.annotation.RequiresPermission;

import java.util.Objects;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;


/**
 * 弹窗工具类
 * <p/>
 * 单例
 * Toast工具
 * 弹窗工具
 *
 * 2020/09/03 DialogFragment必须用Activity作为Ctx
 */
public class NoticeUtil {
    private static final String TAG = "NoticeUtil";
    private static NoticeUtil mInstance;    //单例
    private CustomDialogFragment dialogHelper;  //弹窗助手
    private Toast mToast;   //Toast实例
    private FoxCore core = FoxCore.get();

    /**
     * 静态获取
     */
    public static NoticeUtil get() {
        if (mInstance != null) return mInstance;
        mInstance = new NoticeUtil();
        return mInstance;
    }

    /**
     * 获取上下文
     *
     * @author 狐彻 2020/9/3 15:56
     */
    private Context getContext(){
        return Objects.requireNonNull(FoxCore.get()).getTopActivity();
    }

//    ******↓Toast相关

    /**
     * 显示提示
     *
     * @param resourceId 提示文字资源
     */
    public void showToast(final int resourceId) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null)
                    mToast.cancel();
                mToast = Toast.makeText(getContext(), resourceId, Toast.LENGTH_LONG);
                mToast.show();
            }
        });
    }

    /**
     * 显示提示
     *
     * @param text 文字
     */
    public void showToast(final CharSequence text) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(text)) return;
                if (mToast != null)
                    mToast.cancel();
                mToast = Toast.makeText(getContext(), text, Toast.LENGTH_LONG);
                mToast.show();
            }
        });
    }

    /**
     * 获取弹窗构造器
     */
    public CustomDialogFragment getDialogHelper() {
        if (dialogHelper != null) {
            dismissDialog();
            dialogHelper = null;
        }

        dialogHelper = CustomDialogFragment.get(getContext());
        return dialogHelper;
    }

    /**
     * 加载Dialog
     *
     * @author 狐彻 2020/09/10 14:16
     */
    public CustomDialogFragment showLoadingDialog(){
        return showLoadingDialog("请稍等", null);
    }

    /**
     * 加载Dialog
     *
     * @author 狐彻 2020/09/10 14:17
     */
    public CustomDialogFragment showLoadingDialog(String title){
        return showLoadingDialog(title, null);
    }

    /**
     * 显示网络加载Dialog
     */
    public CustomDialogFragment showLoadingDialog(String title, AlertDialog.OnClickListener cancelClickListener) {
        CustomDialogFragment dialogHelper = getDialogHelper();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ProgressBar bar = new ProgressBar(getContext());
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
            dialogHelper.show(core.getTopActivity().getSupportFragmentManager(), "loading_fragment_dialog");
        else
            Log.e(TAG, "getHelper: call FoxCore#init(Application) first!!!", new IllegalAccessException());

        return dialogHelper;
    }

    /**
     * 简单的警告信息
     *
     * @author binze 2019/10/21 15:08
     * update 2019/12/17 取消自动show()
     */
    public CustomDialogFragment simpleAlertDialog(String title, String message, boolean cancelable
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
     * 带进度条的加载框
     *
     * @author 狐彻 2020/09/10 14:21
     */
    public ProgressDialog progressDialog(String title, int max){
        return new ProgressDialog(title, max);
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
        private android.os.Vibrator vibrator = (android.os.Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        private int amplitude = DEFAULT_AMPLITUDE;  //振幅

        private Vibrator(){}

        /**
         * 设置振幅
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
    public class ProgressDialog {
        private static final String TAG = "ProgressDialog";
        private CustomDialogFragment helper;
        private ProgressBar bar;
        private int max;
        private FoxCore core = FoxCore.get();

        private ProgressDialog(String title, int max) {
            int dp8 = (int) DimenUtil.get().dipToPx(8);
            this.max = max;
            Context ctx = FoxCore.getApplication();
            helper = getDialogHelper();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bar = new ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal);
            bar.setPadding(dp8 * 2, dp8, dp8 * 2, dp8);
            bar.setLayoutParams(params);
            bar.setIndeterminate(false);
            bar.setMax(max);
            helper
                    .title(title)
                    .cancelable(false)
                    .view(bar);
        }

        /**
         * 动画过渡到该数值
         *
         * @author 狐彻 2020/09/10 14:30
         */
        public void progress(int progress){
            progress(progress, true);
        }

        /**
         * 设置当前进度到数值
         *
         * @author 狐彻 2020/09/10 14:29
         */
        public void progress(int progress, boolean animate) {
            if (progress > max) progress = max;
            bar.setProgress(progress, animate);
        }

        /**
         * 提升数值
         *
         * @author 狐彻 2020/09/10 14:31
         */
        public void increase(int count) {
            bar.incrementProgressBy(count);
        }

        /**
         * 设置当前比例
         *
         * @author 狐彻 2020/09/10 14:32
         */
        public void progressByPercent(float percent){
            int progress = (int) (max * percent);
            progress(progress);
        }

        /**
         * 设置当前比例
         *
         * @author 狐彻 2020/09/10 14:32
         */
        public void progressByPercent(float percent, boolean animate){
            int progress = (int) (max * percent);
            progress(progress, animate);
        }

        /**
         * 比例提升
         *
         * @author 狐彻 2020/09/10 14:34
         */
        public void increaseByPercent(float percent){
            int progress = (int) (max * percent);
            increase(progress);
        }


        /**
         * 显示
         *
         * @author 狐彻 2020/09/10 14:31
         */
        public void show() {
            helper.show();
        }
    }
}
