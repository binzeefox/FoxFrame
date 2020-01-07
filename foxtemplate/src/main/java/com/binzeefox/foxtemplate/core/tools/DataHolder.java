package com.binzeefox.foxtemplate.core.tools;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 数据保存类
 *
 * 当数据发生改变时，自动通知所有注册过的回调
 *
 * @author binze
 * 2020/1/6 9:51
 */
public class DataHolder {
    private static final String TAG = "DataHolder";
    private final Map<String, Object> mData = new HashMap<>();
    private final Map<String, Callback> mEntry = new HashMap<>();
    private final ReentrantReadWriteLock mDataLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock mEntryLock = new ReentrantReadWriteLock();

    /**
     * 订阅
     * @author binze 2020/1/6 9:57
     */
    public void submit(String key, Callback callback){
        mEntryLock.writeLock().lock();
        try {
            if (mData.keySet().contains(key)) Log.w(TAG, "submit: 字段冲突，覆盖回调");
            mEntry.put(key, callback);
        } finally {
            mEntryLock.writeLock().unlock();
        }
    }

    /**
     * 接触订阅
     * @author binze 2020/1/6 9:57
     */
    public void unsubmit(String key){
        mEntryLock.writeLock().lock();
        try {
            mEntry.remove(key);
        } finally {
            mEntryLock.writeLock().unlock();
        }
    }

    /**
     * 修改数据
     * @author binze
     * 2020/1/6 9:58
     */
    public void set(String key, Object value){
        mDataLock.writeLock().lock();
        try {
            mData.put(key, value);
            for (Callback callback : mEntry.values()) {
                mEntryLock.readLock().lock();
                try {
                    callback.onCall(key, value);
                } finally {
                    mEntryLock.readLock().unlock();
                }
            }
        } finally {
            mDataLock.writeLock().unlock();
        }
    }

    /**
     * 读数据
     * @author binze 2020/1/6 10:11
     */
    public <T> T read(String key, T defaultValue){
        mDataLock.readLock().lock();
        try {
            T value = (T) mData.get(key);
            return value == null ? defaultValue : value;
        } catch (Exception e){
            Log.e(TAG, "read: ", e);
            return defaultValue;
        } finally {
            mDataLock.readLock().unlock();
        }
    }

    /**
     * 数据变化回调
     * @author binze
     * 2020/1/6 9:57
     */
    public interface Callback{
        void onCall(String key, Object value);
    }
}
