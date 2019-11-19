package com.binzeefox.foxtemplate.utils;

import android.app.Activity;
import android.util.Log;
import android.util.LruCache;

import com.binzeefox.foxtemplate.base.FoxActivity;
import com.binzeefox.foxtemplate.base.FoxApplication;

import java.util.Stack;


/**
 * 活动管理器
 */
public class ActivityCollector {
    private static final String TAG = "ActivityCollector";

    /**
     * 全局的存放数据，在任何一个地方都能取到
     * 最多存放8条信息
     */
    private final LruCache<String, Object> globalData;
    private static ActivityCollector sInstance = new ActivityCollector();
    private static final Stack<FoxActivity> sActivityStack = new Stack<>();

    private ActivityCollector() {
        globalData = new LruCache<String, Object>(8) {
            @Override
            protected int sizeOf(String key, Object value) {
                return 1;
            }
        };
    }

    /**
     * 静态获取单例
     */
    public static ActivityCollector get() {
        synchronized (ActivityCollector.class) {
            return sInstance;
        }
    }

//    ******↑构造方法
//    ******↓公共方法

    /**
     * 获取模拟返回栈
     */
    public static Stack<FoxActivity> getActivityStack() {
        return sActivityStack;
    }

    /**
     * 添加活动
     */
    public void list(FoxActivity activity) {
        sActivityStack.push(activity);
        Log.v(TAG, "registerActivity: "+ activity.getClass().getName());
        logger();
    }

    /**
     * 移除活动
     */
    public void delist(FoxActivity activity) {
        sActivityStack.remove(activity);
        Log.v(TAG, "unRegisterActivity: " + activity.getClass().getName());
        logger();
    }

    /**
     * 获取栈顶Activity
     */
    public FoxActivity getTopActivity() {
        return sActivityStack.peek();
    }

    /**
     * 杀死活动
     */
    public void kill() {
        kill(1);
    }

    /**
     * 杀死所有活动
     */
    public void killAll() {
        kill(sActivityStack.size());
    }

    /**
     * 杀死一定数量活动
     *
     * @param count 从栈顶起需要杀死的数量
     */
    public void kill(int count) {
        if (count <= 0)
            return;
        if (count > sActivityStack.size())
            count = sActivityStack.size();
        while (count != 0) {
            sActivityStack.pop().finish();
            count--;
        }
    }

    /**
     * 添加全局数据
     *
     * @param key   键
     * @param value 值
     */
    public void putGlobalData(String key, Object value) {
        if (getGlobalData(key) != null)
            removeGlobalData(key);
        globalData.put(key, value);
    }

    /**
     * 获取全局数据
     *
     * @param key 键
     * @param <T> 数据类型
     * @return 数据
     */
    public <T> T getGlobalData(String key) {
        return (T) globalData.get(key);
    }

    /**
     * 移除全局数据
     *
     * @param key   键
     * @param <T>   数据类型
     * @return  移除掉的数据
     */
    public <T> T removeGlobalData(String key) {
        return (T) globalData.remove(key);
    }


    public static void logger(){
        if (!FoxApplication.DEV_MODE) return;
        Log.v(TAG, "logger: list current activity stack\n" + printActivityStack() + "\n");
    }

    /**
     * 返回栈内所有activity的字符串
     * @author binze 2019/11/1 9:41
     */
    public static String printActivityStack(){
        StringBuilder sb = new StringBuilder("------------------------start------------------------");
        int i = 0;
        for (Activity activity : sActivityStack){
            String name = activity.getClass().getName();
            sb
                    .append("\n")
                    .append(i).append(": ").append(name);
            i++;
        }
        return sb.append("\n").append("-------------------------end-------------------------").toString();
    }
}
