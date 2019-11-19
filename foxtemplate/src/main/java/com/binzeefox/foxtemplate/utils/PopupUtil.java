package com.binzeefox.foxtemplate.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.binzeefox.foxtemplate.base.FoxApplication;
import com.binzeefox.foxtemplate.customviews.CustomDialogFragment;


/**
 * 弹窗工具类
 * <p/>
 * 单例
 * Toast工具
 * 弹窗工具
 */
public class PopupUtil {
    private static PopupUtil mInstance;    //单例
    private CustomDialogFragment dialogHelper;  //弹窗助手
    private FoxApplication mApp;    //Application 实例
    private Toast mToast;   //Toast实例
//    private Snackbar mSnackbar;   //Snackbar实例    //请在自己工程的工具类里实现

    /**
     * 单例模式
     */
    public static PopupUtil get() {
        if (mInstance != null)
            return mInstance;
        mInstance = new PopupUtil(FoxApplication.get());
        return mInstance;
    }

    /**
     * 构造器
     *
     * @param ctx ctx
     */
    private PopupUtil(Context ctx) {
        //获取全局ApplicationContext防止内存泄漏
        mApp = (FoxApplication) ctx.getApplicationContext();
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
        mToast = Toast.makeText(mApp.getTopActivity(), resourceId, Toast.LENGTH_LONG);
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
        mToast = Toast.makeText(mApp.getTopActivity(), text, Toast.LENGTH_LONG);
        mToast.show();
    }

//    /**
//     * 显示提示
//     *
//     * @param resourceId 提示文字资源
//     */
//    public void showSnackbar(int resourceId) {
//        if (mSnackbar != null)
//            mSnackbar.dismiss();
//        mSnackbar = Snackbar.make(mApp.getTopActivity().getWindow().getDecorView()
//                , resourceId, Snackbar.LENGTH_SHORT);
//        mSnackbar.show();
//    }
//
//    /**
//     * 显示提示
//     *
//     * @param text 文字
//     */
//    public void showSnackbar(CharSequence text) {
//        if (TextUtils.isEmpty(text)) return;
//        if (mSnackbar != null)
//            mSnackbar.dismiss();
//        mSnackbar = Snackbar.make(mApp.getTopActivity().getWindow().getDecorView()
//                , text, Snackbar.LENGTH_SHORT);
//        mSnackbar.show();
//    }

//    ******↓弹窗相关

    /**
     * 获取弹窗构造器
     */
    public CustomDialogFragment getDialogHelper() {
        if (dialogHelper != null) {
            dismissDialog();
            dialogHelper = null;
        }

        dialogHelper = CustomDialogFragment.get(mApp.getTopActivity());
        return dialogHelper;
    }

    /**
     * 显示网络加载Dialog
     */
    public CustomDialogFragment showLoadingDialog(String title, AlertDialog.OnClickListener cancelClickListener) {
        CustomDialogFragment dialogHelper = getDialogHelper();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ProgressBar bar = new ProgressBar(mApp.getTopActivity());
        bar.setPadding(0, 30, 0, 30);
        bar.setLayoutParams(params);
        bar.setIndeterminate(true);
        dialogHelper
                .title(title)
                .cancelable(false)
                .view(bar);
        if (cancelClickListener != null)
            dialogHelper.negativeButton("取消", cancelClickListener);
        dialogHelper.show(mApp.getTopActivity().getSupportFragmentManager());
        return dialogHelper;
    }

    /**
     * 显示简单的警告信息
     * @author binze 2019/10/21 15:08
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
        dialogHelper.show(mApp.getTopActivity().getSupportFragmentManager());
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
     * 显示带进度条网络加载Dialog
     */
    public static class ProgressDialog {
        static ProgressDialog sInstance;
        private CustomDialogFragment helper;
        private ProgressBar bar;
        private int max;

        public static ProgressDialog getInstance(String title, int max) {

            return new ProgressDialog(title, max);
        }

        private ProgressDialog(String title, int max) {
            this.max = max;
            helper = getHelper();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bar = new ProgressBar(FoxApplication.get(), null, android.R.attr.progressBarStyleHorizontal);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bar.setProgress(progress, true);
            } else bar.setProgress(progress);
        }

        public void increase(int count) {
            bar.incrementProgressBy(count);
        }

        public void show() {
            helper.show(FoxApplication.get().getTopActivity().getSupportFragmentManager());
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

            helper = CustomDialogFragment.get(FoxApplication.get().getTopActivity());
            return helper;
        }
    }
}
