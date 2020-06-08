package com.binzeefox.foxframe.tools.dev;

// 线程池基础
//
// corePoolSize: 核心线程数，默认情况下会在线程池中一直存活
// maximumPoolSize: 最大线程数，当活跃线程到达该数目时，后续线程进入队列等待
// keepAliveTime: 非核心线程闲置超时，超时后，闲置的非核心线程将被回收
// workQueue: 任务队列，储存线程
//
// FixedThreadPool: 固定线程数，只有核心线程
// CachedThreadPool: 非固定线程数，只有非核心线程
// ScheduledThreadPool: 核心线程数固定，非核心线程数无限制，常用于执行定时任务和又周期性的任务
// SingleThreadPool: 只有一个核心线程，确保所有任务都在统一线程按顺序执行

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程工具类
 * @author binze
 * 2019/11/28 11:00
 */
public class ThreadUtil implements Closeable {
    private static final String TAG = "ThreadUtil";
    private static ThreadUtil sInstance;
    private ExecutorService mExecutor = Executors.newCachedThreadPool(); //选用Cached线程池

    /**
     * 单例静态获取
     * @author binze 2019/11/28 11:21
     */
    public static ThreadUtil get(){
        if (sInstance == null) {
            synchronized (ThreadUtil.class) {
                if (sInstance != null) return sInstance;
                sInstance = new ThreadUtil();
                return sInstance;
            }
        }
        return sInstance;
    }

    /**
     * 私有化构造器
     * @author binze 2019/11/28 11:21
     */
    private ThreadUtil(){}

    /**
     * 运行线程
     * @author binze 2019/11/28 11:26
     */
    public void execute(Runnable work){
        mExecutor.execute(work);
    }

    /**
     * 关闭线程池，回收资源
     * @author binze 2019/11/28 11:26
     */
    @Override
    public void close(){
        sInstance = null;
        if (mExecutor != null && !mExecutor.isShutdown())
            mExecutor.shutdown();
        mExecutor = null;
    }
}
