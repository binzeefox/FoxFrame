package com.binzeefox.foxframe.tools.dev;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class 相关工具类
 * @author binze
 * 2019/11/19 9:20
 */
public class ClassUtil {
    private static final String TAG = "ClassUtil";

    /**
     * 通过全局变量名获取该变量值
     *
     * 只能获取 get+首字母大写 的方法的数据
     *
     * 说起来。。为啥当初没用直接获取Fields的方法获取数据呢。。。困惑，有空改掉
     * 2019/12/11 ↑ 因为getFields()返回的是public修饰的属性，好了闭嘴吧。。。
     *
     * @param fieldName 字段名
     * @param o 目标数据类
     * @author binze 2019/11/19 9:21
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter);
            return method.invoke(o);
        } catch (Exception e) {
            LogUtil.w(TAG, "getFieldValueByName: ", e);
            return null;
        }
    }

    /**
     * 通过全局变量名设置该变量值
     *
     * 只能设置 set+首字母大写 的方法的数据
     *
     * @param fieldName 字段名
     * @param target 目标数据类
     * @param value 值
     * @author binze 2019/11/19 9:21
     */
    public static void setFieldValueByName(String fieldName, Object target, Object value) {
        if (value == null) return;
        try {
            Class<?> cls = target.getClass();
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "set" + firstLetter + fieldName.substring(1);
            Method method = null;
            for (Method m : cls.getMethods()) {
                if (TextUtils.equals(getter, m.getName())) method = m;
            }
            if (method == null) return;
            String s = value.toString();
            if (TextUtils.isEmpty(s)) return;
            switch (method.getParameterTypes()[0].getSimpleName().toLowerCase()) {
                case "string":
                    method.invoke(target, s);
                    break;
                case "double":
                    method.invoke(target, Double.parseDouble(s));
                    break;
                case "boolean":
                    method.invoke(target, (boolean) value);
                    break;
                case "integer":
                case "int":
                    method.invoke(target, Integer.parseInt(s));
                    break;
                default:
                    break;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogUtil.e(TAG, "getFieldValueByName: ", e);
            e.printStackTrace();
        }
    }
}
