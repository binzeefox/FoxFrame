package com.binzeefox.foxtemplate.base.interfaces;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

/**
 * 视图帮助类
 *
 * {@link #getStringById(int)}  获取TextView的文字，若非TextView则返回null，若为空则返回""
 * {@link #getDoubleById(int)}  获取TextView的双精度浮点数字，若非TextView或值非数字则抛出异常
 * {@link #getIntegerById(int)} 获取TextView的整形数字，若非TextView或值非数字则抛出异常
 * {@link #setErrorById(int, CharSequence)} 通过id设置EditText错误值
 * {@link #setErrorById(int, int)}  通过id设置EditText错误值
 * {@link #setTextById(int, CharSequence)}  通过Id设置TextView文字
 * {@link #setTextById(int, int)}   通过Id设置TextView文字
 * {@link #clearViewById(int)}  清除TextView的文字和状态
 * {@link #checkVisibility(View)}   检查View可见性, 若非Visible则返回false，否则返回true
 * {@link #checkVisibilityById(int)}    检查View可见性, 若非Visible则返回false，否则返回true
 * {@link #checkFieldEmptyById(int)}    检查填写项是否为空
 * {@link #checkEnable(View)}   检查View是否启用
 * {@link #checkEnableById(int)}    检查View是否启用
 * {@link #setViewsVisibility(int, View...)}    批量设置View可见性
 * {@link #setViewsVisibilityByIds(int, int...)}    批量设置View可见性
 * {@link #setViewsEnable(boolean, View...)}    批量设置View可用
 * {@link #setViewsEnableById(boolean, int...)} 批量设置View可用
 *
 * @author binze
 * 2019/12/3 14:52
 */
public interface ViewHelper {
    //EditText, TextView
    String getStringById(@IdRes int id);
    double getDoubleById(@IdRes int id);
    int getIntegerById(@IdRes int id);

    void setErrorById(@IdRes int id, CharSequence error);
    void setErrorById(@IdRes int id, @StringRes int strId);
    void setTextById(@IdRes int id, CharSequence text);
    void setTextById(@IdRes int id, @StringRes int strId);
    void clearViewById(@IdRes int id);

    //State
    boolean checkVisibility(View view);
    boolean checkVisibilityById(@IdRes int id);
    boolean checkFieldEmptyById(@IdRes int id);

    boolean checkEnable(View view);
    boolean checkEnableById(@IdRes int id);

    void setViewsVisibility(int visibility, View... views);
    void setViewsVisibilityByIds(int visibility, @IdRes int... ids);

    void setViewsEnable(boolean enable, View... views);
    void setViewsEnableById(boolean enable, @IdRes int... ids);

}
