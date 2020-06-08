package com.binzeefox.foxframe.tools.dev;

import android.util.Log;


/**
 * 日志工具类
 * <p>
 * 手动更改{@link #CURRENT_CLASS} 的值来改变当前的打印等级
 *
 * @author binze
 * 2019/11/19 9:31
 */
@SuppressWarnings("ALL")
public class LogUtil {
    private static final int CLASS_NOON = -1;   //CURRENT_CLASS为此时，不打印Log
    private static final int CLASS_E = 0;   //CURRENT_CLASS为此时，只打印E
    private static final int CLASS_W = 1;   //CURRENT_CLASS为此时，只打印E和W
    private static final int CLASS_D = 2;   //CURRENT_CLASS为此时，只打印E,W和D
    private static final int CLASS_I = 3;   //CURRENT_CLASS为此时，只打印E,W,D和I
    private static final int CLASS_V = 4;   //CURRENT_CLASS为此时，全部打印

    public static int CURRENT_CLASS = CLASS_V;   //打印指示器

    //V
    public static void v(CharSequence tag, CharSequence text) {
        if (CURRENT_CLASS >= CLASS_V)
            Log.v(tag.toString(), text.toString());
    }

    public static void v(CharSequence tag, CharSequence text, Throwable throwable) {
        if (CURRENT_CLASS >= CLASS_V)
            Log.v(tag.toString(), text.toString(), throwable);
    }

    //D
    public static void d(CharSequence tag, CharSequence text) {
        if (CURRENT_CLASS >= CLASS_D)
            Log.d(tag.toString(), text.toString());
    }

    public static void d(CharSequence tag, CharSequence text, Throwable throwable) {
        if (CURRENT_CLASS >= CLASS_D)
            Log.d(tag.toString(), text.toString(), throwable);
    }

    //I
    public static void i(CharSequence tag, CharSequence text) {
        if (CURRENT_CLASS >= CLASS_I)
            Log.i(tag.toString(), text.toString());
    }

    public static void i(CharSequence tag, CharSequence text, Throwable throwable) {
        if (CURRENT_CLASS >= CLASS_I)
            Log.i(tag.toString(), text.toString(), throwable);
    }

    //W
    public static void w(CharSequence tag, CharSequence text) {
        if (CURRENT_CLASS >= CLASS_W)
            Log.w(tag.toString(), text.toString());
    }

    public static void w(CharSequence tag, CharSequence text, Throwable throwable) {
        if (CURRENT_CLASS >= CLASS_W)
            Log.w(tag.toString(), text.toString(), throwable);
    }

    //E
    public static void e(CharSequence tag, CharSequence text) {
        if (CURRENT_CLASS >= CLASS_E)
            Log.e(tag.toString(), text.toString());
    }

    public static void e(CharSequence tag, CharSequence text, Throwable throwable) {
        if (CURRENT_CLASS >= CLASS_E)
            Log.e(tag.toString(), text.toString(), throwable);
    }
}
