package com.binzeefox.foxframe.core.base;

import android.os.Handler;
import android.os.Looper;

import com.binzeefox.foxframe.tools.dev.ThreadUtil;

/**
 * 懒加载碎片
 *
 * @author binze
 * 2020/1/6 9:45
 */
public abstract class BaseLazyFragment extends FoxFragment {
    private static final String TAG = "BaseLazyFragment";
    private boolean isLoaded = false;

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {    //未加载过，则在线程中调用onLoad()
//            ThreadUtil.get().execute(new Runnable() {
//                @Override
//                public void run() {
//                    onLoad();
//                }
//            });
            onLoad();
        }
        isLoaded = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //若不将其在此置false，onDestroy后可能类没有被回收，而导致再次加载时会失败
        isLoaded = false;
    }

    /**
     * 手动控制该Fragment是为已加载状态
     *
     * @author 狐彻 2020/09/18 15:00
     */
    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    /**
     * fragment第一次加载
     * <p>
     * 在Fragment第一次可见时调用
     * 该方法运行在其它线程
     *
     * @author binze 2020/1/6 9:43
     */
    protected abstract void onLoad();

    /**
     * 主线程运行
     *
     * @author binze 2020/1/6 9:43
     */
    protected void runOnUiThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }
}
