package com.binzeefox.foxframe.core.base;

import android.os.Handler;
import android.os.Looper;

import com.binzeefox.foxframe.tools.dev.ThreadUtil;

/**
 * 懒加载碎片
 * @author binze
 * 2020/1/6 9:45
 */
public abstract class BaseLazyFragment extends FoxFragment {
    private static final String TAG = "BaseLazyFragment";
    private boolean isLoaded = false;

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            ThreadUtil.get().execute(new Runnable() {
                @Override
                public void run() {
                    onLoad();
                }
            });
        }
        isLoaded = true;
    }

    /**
     * fragment第一次加载
     *
     * 在Fragment第一次可见时调用
     * 该方法自动运行在其它线程
     * @author binze 2020/1/6 9:43
     */
    protected abstract void onLoad();
    
    /**
     * 主线程运行
     * @author binze 2020/1/6 9:43
     */
    protected void runOnUiThread(Runnable runnable){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }
}
