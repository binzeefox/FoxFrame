package com.binzeefox.foxtemplate.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * 自定义弹窗碎片
 *
 * 通过Builder类似方法进行构造
 * 内部持有一个AlertDialog.Builder
 */
public class CustomDialogFragment extends AppCompatDialogFragment {

    private static AlertDialog.Builder mBuilder;

    /**
     * 空构造方法
     */
    public CustomDialogFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mBuilder.create();
    }

//    ****************↓公共方法

    /**
     * 静态获取
     */
    public static CustomDialogFragment get(Context ctx) {
        mBuilder = new AlertDialog.Builder(ctx);
        return new CustomDialogFragment();
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public CustomDialogFragment title(CharSequence title) {
        mBuilder.setTitle(title);
        return this;
    }

    /**
     * 设置文字
     *
     * @param message 内容
     */
    public CustomDialogFragment message(CharSequence message) {
        mBuilder.setMessage(message);
        return this;
    }

    /**
     * 设置自定义布局
     *
     * @param view 布局
     */
    public CustomDialogFragment view(View view) {
        mBuilder.setView(view);
        return this;
    }

    /**
     * 设置自定义布局
     *
     * @param viewResource 布局资源id
     */
    public CustomDialogFragment view(int viewResource) {
        mBuilder.setView(viewResource);
        return this;
    }

    /**
     * 设置是否边缘取消
     *
     * @param isCancelable 是否
     */
    public CustomDialogFragment cancelable(boolean isCancelable) {
        setCancelable(isCancelable);
        return this;
    }

    /**
     * 设置积极按钮
     *
     * @param resource 按钮文字
     * @param listener 点击事件
     */
    public CustomDialogFragment positiveButton(int resource, AlertDialog.OnClickListener listener) {
        mBuilder.setPositiveButton(resource, listener);
        return this;
    }

    /**
     * 设置积极按钮
     *
     * @param text     按钮文字
     * @param listener 点击事件
     */
    public CustomDialogFragment positiveButton(String text, AlertDialog.OnClickListener listener) {
        mBuilder.setPositiveButton(text, listener);
        return this;
    }

    /**
     * 设置消极按钮
     *
     * @param resource 按钮文字
     * @param listener 点击事件
     */
    public CustomDialogFragment negativeButton(int resource, AlertDialog.OnClickListener listener) {
        mBuilder.setNegativeButton(resource, listener);
        return this;
    }

    /**
     * 设置消极按钮
     *
     * @param text     按钮文字
     * @param listener 点击事件
     */
    public CustomDialogFragment negativeButton(String text, AlertDialog.OnClickListener listener) {
        mBuilder.setNegativeButton(text, listener);
        return this;
    }

    /**
     * 设置销毁监听
     *
     * @param listener 监听器
     */
    public CustomDialogFragment onDismissListener(DialogInterface.OnDismissListener listener) {
        mBuilder.setOnDismissListener(listener);
        return this;
    }

    public void show(FragmentManager manager) {
        try {
            super.show(manager, this.getClass().getSimpleName());
        }catch (IllegalStateException e){
            Log.e("CustomDialogFragment", e.getMessage(), e);
        }
    }
}
